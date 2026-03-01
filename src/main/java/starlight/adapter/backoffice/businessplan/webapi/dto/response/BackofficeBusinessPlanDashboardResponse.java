package starlight.adapter.backoffice.businessplan.webapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanDashboardResult;
import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDate;
import java.util.List;

public record BackofficeBusinessPlanDashboardResponse(
        StatsResponse stats,
        List<TrendPointResponse> trend,
        List<StatusDistributionItemResponse> statusDistribution,
        List<ScoreDistributionItemResponse> scoreDistribution
) {
    public static BackofficeBusinessPlanDashboardResponse from(BackofficeBusinessPlanDashboardResult result) {
        return new BackofficeBusinessPlanDashboardResponse(
                StatsResponse.from(result.stats()),
                result.trend().stream().map(TrendPointResponse::from).toList(),
                result.statusDistribution().stream().map(StatusDistributionItemResponse::from).toList(),
                result.scoreDistribution().stream().map(ScoreDistributionItemResponse::from).toList()
        );
    }

    public record StatsResponse(
            long totalCount,
            long scoredCount,
            long unscoredCount,
            Double averageScore
    ) {
        public static StatsResponse from(BackofficeBusinessPlanDashboardResult.StatsResult result) {
            return new StatsResponse(
                    result.totalCount(),
                    result.scoredCount(),
                    result.unscoredCount(),
                    result.averageScore()
            );
        }
    }

    public record TrendPointResponse(
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
            LocalDate date,
            long count
    ) {
        public static TrendPointResponse from(BackofficeBusinessPlanDashboardResult.TrendPointResult result) {
            return new TrendPointResponse(result.date(), result.count());
        }
    }

    public record StatusDistributionItemResponse(
            PlanStatus status,
            String label,
            long count,
            double ratio
    ) {
        public static StatusDistributionItemResponse from(BackofficeBusinessPlanDashboardResult.StatusDistributionItemResult result) {
            return new StatusDistributionItemResponse(
                    result.status(),
                    result.label(),
                    result.count(),
                    result.ratio()
            );
        }
    }

    public record ScoreDistributionItemResponse(
            String bucket,
            String label,
            long count,
            double ratio
    ) {
        public static ScoreDistributionItemResponse from(BackofficeBusinessPlanDashboardResult.ScoreDistributionItemResult result) {
            return new ScoreDistributionItemResponse(
                    result.bucket(),
                    result.label(),
                    result.count(),
                    result.ratio()
            );
        }
    }
}
