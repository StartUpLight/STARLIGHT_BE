package starlight.application.notification.provided.dto.input;

public record NotificationSendInput(
        Long memberId,
        String type,
        String title,
        String message,
        Long referenceId
) {
    public static NotificationSendInput of(
            Long memberId,
            String type,
            String title,
            String message,
            Long referenceId
    ) {
        return new NotificationSendInput(
                memberId,
                type,
                title,
                message,
                referenceId
        );
    }
}
