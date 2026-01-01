package starlight.application.expert.provided.dto;

public record ExpertAiReportBusinessPlanResult(
        Long businessPlanId,
        String businessPlanTitle,
        Long requestCount,
        boolean isOver70
) {
}
