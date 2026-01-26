package starlight.application.backoffice.expert.provided.dto.result;

public record BackofficeExpertCreateResult(
        Long id
) {
    public static BackofficeExpertCreateResult from(Long id) {
        return new BackofficeExpertCreateResult(id);
    }
}
