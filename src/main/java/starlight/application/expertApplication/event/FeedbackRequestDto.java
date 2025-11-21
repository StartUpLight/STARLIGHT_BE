package starlight.application.expertApplication.event;

public record FeedbackRequestDto(
        String mentorEmail,

        String mentorName,

        String menteeName,

        String businessPlanTitle,

        String feedbackDeadline,

        String feedbackUrl,

        byte[] attachedFile,

        String filename
) {
    public static FeedbackRequestDto of(
            String mentorEmail, String mentorName, String menteeName, String businessPlanTitle,
            String feedbackDeadline, String feedbackUrl, byte[] attachedFile, String filename
    ) {
        return new FeedbackRequestDto(
                mentorEmail, mentorName, menteeName, businessPlanTitle,
                feedbackDeadline, feedbackUrl, attachedFile, filename
        );
    }
}
