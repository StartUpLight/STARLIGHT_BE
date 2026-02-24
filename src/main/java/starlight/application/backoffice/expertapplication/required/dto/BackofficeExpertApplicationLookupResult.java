package starlight.application.backoffice.expertapplication.required.dto;

import starlight.domain.expertReport.enumerate.SubmitStatus;

import java.time.LocalDateTime;

public record BackofficeExpertApplicationLookupResult(
        Long applicationId,
        Long expertId,
        LocalDateTime requestedAt,
        SubmitStatus submitStatus,
        LocalDateTime feedbackUpdatedAt,
        String feedbackSummary
) {
    public static BackofficeExpertApplicationLookupResult of(
            Long applicationId,
            Long expertId,
            LocalDateTime requestedAt,
            SubmitStatus submitStatus,
            LocalDateTime feedbackUpdatedAt,
            String feedbackSummary
    ) {
        return new BackofficeExpertApplicationLookupResult(
                applicationId,
                expertId,
                requestedAt,
                submitStatus,
                feedbackUpdatedAt,
                feedbackSummary
        );
    }
}
