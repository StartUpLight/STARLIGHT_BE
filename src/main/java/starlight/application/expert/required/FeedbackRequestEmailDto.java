package starlight.application.expert.required;

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
            String baseUrl
    ) {
        String deadline = LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_DATE);
        String url = buildFeedbackUrl(baseUrl, plan.getId(), expert.getId());
        return new FeedbackRequestEmailDto(
                expert.getEmail(),
                expert.getName(),
                menteeName,
                plan.getTitle(),
                deadline,
                url,
                attachedFile,
                filename
        );
    }

    private static String buildFeedbackUrl(String baseUrl, long planId, long mentorId) {
        String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalized + "/feedback/" + planId + "/" + mentorId;
    }
}
