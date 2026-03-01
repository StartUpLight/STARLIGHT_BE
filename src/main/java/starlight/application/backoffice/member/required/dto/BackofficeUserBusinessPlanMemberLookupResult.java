package starlight.application.backoffice.member.required.dto;

public record BackofficeUserBusinessPlanMemberLookupResult(
        Long userId,
        Long businessPlanId
) {
    public static BackofficeUserBusinessPlanMemberLookupResult of(Long userId, Long businessPlanId) {
        return new BackofficeUserBusinessPlanMemberLookupResult(userId, businessPlanId);
    }
}
