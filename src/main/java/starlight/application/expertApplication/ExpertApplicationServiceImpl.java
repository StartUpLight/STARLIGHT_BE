package starlight.application.expertApplication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.expertApplication.provided.ExpertApplicationService;
import starlight.application.expertApplication.required.EmailSender;
import starlight.application.expertApplication.required.dto.FeedbackRequestEmailDto;
import starlight.application.expert.required.ExpertQuery;
import starlight.application.expertApplication.required.ExpertApplicationQuery;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expertApplication.entity.ExpertApplication;
import starlight.domain.expertApplication.exception.ExpertApplicationErrorType;
import starlight.domain.expertApplication.exception.ExpertApplicationException;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpertApplicationServiceImpl implements ExpertApplicationService {

    private final EmailSender emailSender;
    private final ExpertQuery expertQuery;
    private final BusinessPlanQuery planQuery;
    private final ExpertApplicationQuery applicationQuery;

    @Override
    @Transactional
    public void requestFeedback(Long expertId, Long planId, MultipartFile file, String menteeName) {
        BusinessPlan plan = planQuery.getOrThrow(planId);
        Expert expert = expertQuery.getOrThrow(expertId);

        registerApplicationRecord(planId, expertId);

        sendFeedbackEmail(expert, plan, file, menteeName);
    }

    public void registerApplicationRecord(Long expertId, Long planId) {
        if (applicationQuery.existsByExpertIdAndBusinessPlanId(expertId, planId)) {
            throw new ExpertApplicationException(ExpertApplicationErrorType.APPLICATION_ALREADY_EXISTS);
        }

        ExpertApplication application = ExpertApplication.create(planId, expertId);
        applicationQuery.save(application);
    }

    private String generateFilename(MultipartFile file, BusinessPlan plan, String menteeName) {
        String originalFilename = file.getOriginalFilename();

        if (originalFilename != null && !originalFilename.isBlank()) {
            return originalFilename;
        }

        return String.format("[사업계획서]%s_%s.pdf", plan.getTitle(), menteeName);
    }

    private void sendFeedbackEmail(Expert expert, BusinessPlan plan, MultipartFile file, String menteeName) {
        try {
            if (file.getSize() > 20 * 1024 * 1024) {
                throw new ExpertApplicationException(ExpertApplicationErrorType.FILE_SIZE_EXCEEDED);
            }

            if (file.getContentType() == null || !file.getContentType().equals("application/pdf")) {
                throw new ExpertApplicationException(ExpertApplicationErrorType.UNSUPPORTED_FILE_TYPE);
            }

            byte[] fileBytes = file.getBytes();
            String filename = generateFilename(file, plan, menteeName);
            String feedbackUrl = buildFeedbackRequestUrl(expert.getId(), plan.getId());

            FeedbackRequestEmailDto dto = FeedbackRequestEmailDto.fromDomain(
                    expert, menteeName, plan, fileBytes, filename, feedbackUrl
            );

            //TODO: 이메일 전송
            // 1. 비동기 처리 고려 2. 재시도 로직 추가 검토 3. 에러 핸들링 추가
            emailSender.sendFeedbackRequestMail(dto);

        } catch (IOException e) {
            log.error("Failed to read file for feedback request", e);
            throw new ExpertApplicationException(ExpertApplicationErrorType.FILE_READ_ERROR);
        }
    }

    private String buildFeedbackRequestUrl(Long expertId, Long planId) {
        //TODO: 해쉬값 기반으로 변경
        return "https://www.starlight.com/feedback?expertId=" + expertId + "&planId=" + planId;
    }
}
