package starlight.application.expertApplication.required.dto;

import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.expert.entity.Expert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record FeedbackRequestEmailDto(
        String mentorEmail,

        String mentorName,

        String menteeName,

        String businessPlanTitle,

        String feedbackDeadline,

        String feedbackUrl,

        byte[] attachedFile,

        String filename
) {
    public static FeedbackRequestEmailDto fromDomain(
            Expert expert,
            String menteeName,
            BusinessPlan plan,
            byte[] attachedFile,
            String filename,
            String requestUrl
    ) {
        String deadline = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);
        return new FeedbackRequestEmailDto(
                expert.getEmail(),
                expert.getName(),
                menteeName,
                plan.getTitle(),
                deadline,
                requestUrl,
                attachedFile,
                filename
        );
    }
}
