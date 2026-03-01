package starlight.application.backoffice.order.required.dto;

public record BackofficeOrderMemberLookupResult(
        Long memberId,
        String name,
        String email
) {
    public static BackofficeOrderMemberLookupResult of(Long memberId, String name, String email) {
        return new BackofficeOrderMemberLookupResult(memberId, name, email);
    }
}
