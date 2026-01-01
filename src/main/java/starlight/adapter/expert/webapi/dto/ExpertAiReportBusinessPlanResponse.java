package starlight.adapter.expert.webapi.dto;

import starlight.application.expert.provided.dto.ExpertAiReportBusinessPlanResult;

import java.util.List;

public record ExpertAiReportBusinessPlanResponse(
        Long businessPlanId,
        String businessPlanTitle,
        Long requestCount,
        boolean isOver70
) {
    public static ExpertAiReportBusinessPlanResponse from(ExpertAiReportBusinessPlanResult result) {
        return new ExpertAiReportBusinessPlanResponse(
                result.businessPlanId(),
                result.businessPlanTitle(),
                result.requestCount(),
                result.isOver70()
        );
    }

    public static List<ExpertAiReportBusinessPlanResponse> fromAll(List<ExpertAiReportBusinessPlanResult> results) {
        return results.stream()
                .map(ExpertAiReportBusinessPlanResponse::from)
                .toList();
    }
}
