package starlight.adapter.backoffice.expertapplication.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.application.backoffice.expertapplication.provided.dto.result.BackofficeBusinessPlanExpertApplicationsResult;

import java.time.LocalDateTime;
import java.util.List;

public record BackofficeBusinessPlanExpertApplicationsResponse(
        Long businessPlanId,
        List<ExpertApplicationResponse> expertApplications
) {
    public static BackofficeBusinessPlanExpertApplicationsResponse from(
            BackofficeBusinessPlanExpertApplicationsResult result
    ) {
        return new BackofficeBusinessPlanExpertApplicationsResponse(
                result.businessPlanId(),
                result.expertApplications().stream().map(ExpertApplicationResponse::from).toList()
        );
    }

    public record ExpertApplicationResponse(
            Long applicationId,
            Long expertId,
            String expertName,
            String status,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime requestedAt,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
            LocalDateTime feedbackUpdatedAt,
            Integer feedbackScore,
            String feedbackSummary,
            List<String> strengths,
            List<String> weaknesses
    ) {
        public static ExpertApplicationResponse from(
                BackofficeBusinessPlanExpertApplicationsResult.ExpertApplicationResult result
        ) {
            return new ExpertApplicationResponse(
                    result.applicationId(),
                    result.expertId(),
                    result.expertName(),
                    result.status(),
                    result.requestedAt(),
                    result.feedbackUpdatedAt(),
                    result.feedbackScore(),
                    result.feedbackSummary(),
                    result.strengths(),
                    result.weaknesses()
            );
        }
    }
}
