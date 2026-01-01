package starlight.application.expertApplication.event;

public record FeedbackRequestInput(
        String mentorEmail,

        String mentorName,

        String menteeName,

        String businessPlanTitle,

        String feedbackDeadline,

        String feedbackUrl,

        byte[] attachedFile,

        String filename
) {
    public static FeedbackRequestInput of(
            String mentorEmail, String mentorName, String menteeName, String businessPlanTitle,
            String feedbackDeadline, String feedbackUrl, byte[] attachedFile, String filename
    ) {
        return new FeedbackRequestInput(
                mentorEmail, mentorName, menteeName, businessPlanTitle,
                feedbackDeadline, feedbackUrl, attachedFile, filename
        );
    }
}
