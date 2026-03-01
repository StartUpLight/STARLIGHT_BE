package starlight.application.backoffice.expertapplication.provided.dto.result;

import java.time.LocalDateTime;
import java.util.List;

public record BackofficeBusinessPlanExpertApplicationsResult(
        Long businessPlanId,
        List<ExpertApplicationResult> expertApplications
) {
    public static BackofficeBusinessPlanExpertApplicationsResult of(
            Long businessPlanId,
            List<ExpertApplicationResult> expertApplications
    ) {
        return new BackofficeBusinessPlanExpertApplicationsResult(businessPlanId, expertApplications);
    }

    public record ExpertApplicationResult(
            Long applicationId,
            Long expertId,
            String expertName,
            String status,
            LocalDateTime requestedAt,
            LocalDateTime feedbackUpdatedAt,
            Integer feedbackScore,
            String feedbackSummary,
            List<String> strengths,
            List<String> weaknesses
    ) {
        public static ExpertApplicationResult of(
                Long applicationId,
                Long expertId,
                String expertName,
                String status,
                LocalDateTime requestedAt,
                LocalDateTime feedbackUpdatedAt,
                Integer feedbackScore,
                String feedbackSummary,
                List<String> strengths,
                List<String> weaknesses
        ) {
            return new ExpertApplicationResult(
                    applicationId,
                    expertId,
                    expertName,
                    status,
                    requestedAt,
                    feedbackUpdatedAt,
                    feedbackScore,
                    feedbackSummary,
                    strengths,
                    weaknesses
            );
        }
    }
}
