package starlight.application.expertApplicaiton;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.expertApplicaiton.provided.ExpertApplicationService;
import starlight.application.expertApplicaiton.required.EmailSender;
import starlight.application.expertApplicaiton.required.dto.FeedbackRequestEmailDto;
import starlight.application.expert.required.ExpertQuery;
import starlight.application.expertApplicaiton.required.ExpertApplicationQuery;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expertApplication.entity.ExpertApplication;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ExpertApplicationServiceImpl implements ExpertApplicationService {

    private final EmailSender emailSender;
    private final ExpertQuery expertQuery;
    private final BusinessPlanQuery businessPlanQuery;
    private final ExpertApplicationQuery expertApplicationFinder;

    @Override
    public void requestFeedback(Long expertId, Long planId, MultipartFile file, String menteeName) throws IOException {

        ExpertApplication application = ExpertApplication.create(planId, expertId);
        expertApplicationFinder.save(application);

        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);
        Expert expert = expertQuery.getOrThrow(expertId);
        String feedbackUrl = buildFeedbackRequestUrl(expertId, planId);

        byte[] fileBytes = file.getBytes();
        String filename = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? file.getOriginalFilename()
                : "[사업 계획서]" + plan.getTitle() + "_" + menteeName + ".pdf";

        FeedbackRequestEmailDto dto = FeedbackRequestEmailDto.fromDomain(
                expert, menteeName, plan, fileBytes, filename, feedbackUrl
        );

        emailSender.sendFeedbackRequestMail(dto);
    }

    private String buildFeedbackRequestUrl(Long expertId, Long planId) {
        //TODO: 해쉬값 기반으로 변경
        return "https://www.starlight.com/feedback?expertId=" + expertId + "&planId=" + planId;
    }
}
