package starlight.application.backoffice.expert.provided.dto.input;

public record BackofficeExpertProfileImageUpdateInput(
        Long expertId,
        String profileImageUrl
) {
    public static BackofficeExpertProfileImageUpdateInput of(Long expertId, String profileImageUrl) {
        return new BackofficeExpertProfileImageUpdateInput(expertId, profileImageUrl);
    }
}
