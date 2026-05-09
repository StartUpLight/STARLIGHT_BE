package starlight.application.expertApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.expertApplication.event.FeedbackRequestInput;
import starlight.application.expertApplication.provided.ExpertApplicationCommandUseCase;
import starlight.application.expertApplication.required.ExpertLookupPort;
import starlight.application.expertApplication.required.ExpertApplicationQueryPort;
import starlight.application.expertReport.provided.ExpertReportUseCase;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.businessplan.exception.BusinessPlanException;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.exception.ExpertException;
import starlight.domain.expertApplication.entity.ExpertApplication;
import starlight.domain.expertApplication.exception.ExpertApplicationErrorType;
import starlight.domain.expertApplication.exception.ExpertApplicationException;

import java.net.URI;
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

    @Value("${feedback-token.expiration-date}")
    private Long FEEDBACK_DEADLINE_DAYS = 7L;

    @Override
    @Transactional
    public void requestFeedback(Long expertId, Long planId, String pdfUrl, String menteeName) {
        BusinessPlan plan = planQuery.findByIdOrThrow(planId);
        String planFileUrl = resolvePlanFileUrl(plan, pdfUrl);

        try {
            Expert expert = expertLookupPort.findByIdOrThrow(expertId);

            plan.updateStatus(PlanStatus.EXPERT_MATCHED);

            registerApplicationRecord(expertId, planId);

            publishEmailEvent(expert, plan, planFileUrl, menteeName);
        } catch (ExpertApplicationException | BusinessPlanException | ExpertException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to request Feedback. planId={}, expertId={}", planId, expertId, e);
            throw new ExpertApplicationException(ExpertApplicationErrorType.EXPERT_FEEDBACK_REQUEST_FAILED);
        }
    }

    public void registerApplicationRecord(Long expertId, Long planId) {
        if (applicationQueryPort.existsByExpertIdAndBusinessPlanId(expertId, planId)) {
            throw new ExpertApplicationException(ExpertApplicationErrorType.APPLICATION_ALREADY_EXISTS);
        }

        ExpertApplication application = ExpertApplication.create(planId, expertId);
        applicationQueryPort.save(application);
    }

    private String resolvePlanFileUrl(BusinessPlan plan, String pdfUrl) {
        if (StringUtils.hasText(pdfUrl)) {
            return validatePdfUrl(pdfUrl);
        }

        if (plan.isPdfBased() && StringUtils.hasText(plan.getPdfUrl())) {
            return validatePdfUrl(plan.getPdfUrl());
        }

        throw new ExpertApplicationException(ExpertApplicationErrorType.INVALID_PDF_URL);
    }

    private String validatePdfUrl(String pdfUrl) {
        String trimmedUrl = pdfUrl.trim();

        try {
            URI uri = URI.create(trimmedUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !StringUtils.hasText(uri.getHost())) {
                throw new ExpertApplicationException(ExpertApplicationErrorType.INVALID_PDF_URL);
            }
            return trimmedUrl;
        } catch (IllegalArgumentException exception) {
            throw new ExpertApplicationException(ExpertApplicationErrorType.INVALID_PDF_URL);
        }
    }

    protected void publishEmailEvent(Expert expert, BusinessPlan plan, String planFileUrl, String menteeName) {
        String feedbackUrl = buildFeedbackRequestUrl(expert.getId(), plan.getId());

        FeedbackRequestInput event = FeedbackRequestInput.of(
                expert.getEmail(),
                expert.getName(),
                menteeName,
                plan.getTitle(),
                LocalDate.now().plusDays(FEEDBACK_DEADLINE_DAYS).format(DateTimeFormatter.ISO_DATE),
                feedbackUrl,
                planFileUrl
        );

        log.info("[EMAIL] publishing FeedbackRequestEvent expertId={}, planId={}", expert.getId(), plan.getId());

        eventPublisher.publishEvent(event);
    }

    private String buildFeedbackRequestUrl(Long expertId, Long planId) {
        return expertReportUseCase.createExpertReportLink(expertId, planId);
    }
}
