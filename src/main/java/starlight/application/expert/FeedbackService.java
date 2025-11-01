package starlight.application.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.expert.required.EmailSender;
import starlight.application.expert.required.FeedbackRequestEmailDto;
import starlight.application.expert.required.ExpertQuery;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.expert.entity.Expert;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final EmailSender emailSender;
    private final ExpertQuery expertQuery;
    private final BusinessPlanQuery businessPlanQuery;

    @Transactional(readOnly = true)
    public void requestFeedback(Long mentorId, Long planId, MultipartFile file, String menteeName) throws IOException {

        BusinessPlan plan   = businessPlanQuery.getOrThrow(planId);
        Expert expert = expertQuery.getOrThrow(mentorId);

        byte[] fileBytes = file.getBytes();
        String filename = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? "business-plan-" + planId + ".pdf"
                : file.getOriginalFilename();

        FeedbackRequestEmailDto dto = FeedbackRequestEmailDto.fromDomain(
                expert, menteeName, plan, fileBytes, filename, "www.starlight.com"
        );

        emailSender.sendFeedbackRequestMail(dto);
    }
}
