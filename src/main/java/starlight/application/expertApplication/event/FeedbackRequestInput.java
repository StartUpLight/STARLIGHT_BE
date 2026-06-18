package starlight.application.expertApplication.event;

public record FeedbackRequestInput(
        String mentorEmail,

        String mentorName,

        String menteeName,

        String businessPlanTitle,

        String feedbackDeadline,

        String feedbackUrl,

        String planFileUrl
) {
    public static FeedbackRequestInput of(
            String mentorEmail, String mentorName, String menteeName, String businessPlanTitle,
            String feedbackDeadline, String feedbackUrl, String planFileUrl
    ) {
        return new FeedbackRequestInput(
                mentorEmail, mentorName, menteeName, businessPlanTitle,
                feedbackDeadline, feedbackUrl, planFileUrl
        );
    }
}
