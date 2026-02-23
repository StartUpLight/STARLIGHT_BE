package starlight.application.expertApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.expertApplication.event.FeedbackRequestInput;
import starlight.application.expertApplication.provided.ExpertApplicationCommandUseCase;
import starlight.application.expertApplication.required.ExpertLookupPort;
import starlight.application.expertApplication.required.ExpertApplicationQueryPort;
import starlight.application.expertApplication.required.PdfDownloadPort;
import starlight.application.expertReport.provided.ExpertReportUseCase;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.businessplan.exception.BusinessPlanException;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.exception.ExpertException;
import starlight.domain.expertApplication.entity.ExpertApplication;
import starlight.domain.expertApplication.exception.ExpertApplicationErrorType;
import starlight.domain.expertApplication.exception.ExpertApplicationException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpertApplicationCommandService implements ExpertApplicationCommandUseCase {

    private final ExpertLookupPort expertLookupPort;
    private final BusinessPlanQueryPort planQuery;
    private final ExpertApplicationQueryPort applicationQueryPort;
    private final ApplicationEventPublisher eventPublisher;
    private final ExpertReportUseCase expertReportUseCase;
    private final PdfDownloadPort pdfDownloadPort;

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final String ALLOWED_CONTENT_TYPE = "application/pdf";

    @Value("${feedback-token.expiration-date}")
    private Long FEEDBACK_DEADLINE_DAYS = 7L;

    @Override
    @Transactional
    public void requestFeedback(Long expertId, Long planId, MultipartFile file, String menteeName) {
        BusinessPlan plan = planQuery.findByIdOrThrow(planId);

        final byte[] fileBytes;
        final String filename;

        if (plan.isPdfBased()) {
            fileBytes = pdfDownloadPort.downloadFromUrl(plan.getPdfUrl());
            filename = generateFilenameForPdfPlan(plan, menteeName);
        } else {
            validateFile(file);
            try {
                fileBytes = file.getBytes();
            } catch (IOException e) {
                log.error("Failed to read file. planId={}, expertId={}", planId, expertId, e);
                throw new ExpertApplicationException(ExpertApplicationErrorType.FILE_READ_ERROR);
            }
            filename = generateFilename(file, plan, menteeName);
        }

        try {
            Expert expert = expertLookupPort.findByIdOrThrow(expertId);

            plan.updateStatus(PlanStatus.EXPERT_MATCHED);

            registerApplicationRecord(expertId, planId);

            publishEmailEvent(expert, plan, fileBytes, filename, menteeName);
        } catch (ExpertApplicationException | BusinessPlanException | ExpertException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to request Feedback. planId={}, expertId={}", planId, expertId, e);
            throw new ExpertApplicationException(ExpertApplicationErrorType.EXPERT_FEEDBACK_REQUEST_FAILED);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ExpertApplicationException(ExpertApplicationErrorType.EMPTY_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ExpertApplicationException(ExpertApplicationErrorType.FILE_SIZE_EXCEEDED);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(ALLOWED_CONTENT_TYPE)) {
            throw new ExpertApplicationException(ExpertApplicationErrorType.UNSUPPORTED_FILE_TYPE);
        }
    }

    public void registerApplicationRecord(Long expertId, Long planId) {
        if (applicationQueryPort.existsByExpertIdAndBusinessPlanId(expertId, planId)) {
            throw new ExpertApplicationException(ExpertApplicationErrorType.APPLICATION_ALREADY_EXISTS);
        }

        ExpertApplication application = ExpertApplication.create(planId, expertId);
        applicationQueryPort.save(application);
    }

    private String generateFilename(MultipartFile file, BusinessPlan plan, String menteeName) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename != null && !originalFilename.isBlank()) {
            return originalFilename;
        }

        return generateFilenameForPdfPlan(plan, menteeName);
    }

    private String generateFilenameForPdfPlan(BusinessPlan plan, String menteeName) {
        return String.format("[사업계획서]%s_%s.pdf", plan.getTitle(), menteeName);
    }

    protected void publishEmailEvent(Expert expert, BusinessPlan plan, byte[] fileBytes, String filename, String menteeName) {
        String feedbackUrl = buildFeedbackRequestUrl(expert.getId(), plan.getId());

        FeedbackRequestInput event = FeedbackRequestInput.of(
                expert.getEmail(),
                expert.getName(),
                menteeName,
                plan.getTitle(),
                LocalDate.now().plusDays(FEEDBACK_DEADLINE_DAYS).format(DateTimeFormatter.ISO_DATE),
                feedbackUrl,
                fileBytes,
                filename
        );

        log.info("[EMAIL] publishing FeedbackRequestEvent expertId={}, planId={}", expert.getId(), plan.getId());

        eventPublisher.publishEvent(event);
    }

    private String buildFeedbackRequestUrl(Long expertId, Long planId) {
        return expertReportUseCase.createExpertReportLink(expertId, planId);
    }
}
