package starlight.shared.dto.infrastructure;

public record ClovaStudioResponse(
        Status status,
        Result result
) {
    public record Status(
            int code,
            String message
    ) {}

    public record Result(
            Message message,
            String finishReason,
            long created,
            long seed,
            Usage usage
    ) {}

    public record Usage(
            int promptTokens,
            int completionTokens,
            int totalTokens
    ) {}

    public record Message(
            String role,
            String content
    ) {}
}