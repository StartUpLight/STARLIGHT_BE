package starlight.application.backoffice.businessplan.provided.dto.result;

import starlight.domain.businessplan.enumerate.PlanStatus;

import java.time.LocalDate;
import java.util.List;

public record BackofficeBusinessPlanDashboardResult(
        StatsResult stats,
        List<TrendPointResult> trend,
        List<StatusDistributionItemResult> statusDistribution,
        List<ScoreDistributionItemResult> scoreDistribution
) {
    public static BackofficeBusinessPlanDashboardResult of(
            StatsResult stats,
            List<TrendPointResult> trend,
            List<StatusDistributionItemResult> statusDistribution,
            List<ScoreDistributionItemResult> scoreDistribution
    ) {
        return new BackofficeBusinessPlanDashboardResult(stats, trend, statusDistribution, scoreDistribution);
    }

    public record StatsResult(
            long totalCount,
            long scoredCount,
            long unscoredCount,
            Double averageScore
    ) {
        public static StatsResult of(long totalCount, long scoredCount, long unscoredCount, Double averageScore) {
            return new StatsResult(totalCount, scoredCount, unscoredCount, averageScore);
        }
    }

    public record TrendPointResult(
            LocalDate date,
            long count
    ) {
        public static TrendPointResult of(LocalDate date, long count) {
            return new TrendPointResult(date, count);
        }
    }

    public record StatusDistributionItemResult(
            PlanStatus status,
            String label,
            long count,
            double ratio
    ) {
        public static StatusDistributionItemResult of(PlanStatus status, String label, long count, double ratio) {
            return new StatusDistributionItemResult(status, label, count, ratio);
        }
    }

    public record ScoreDistributionItemResult(
            String bucket,
            String label,
            long count,
            double ratio
    ) {
        public static ScoreDistributionItemResult of(String bucket, String label, long count, double ratio) {
            return new ScoreDistributionItemResult(bucket, label, count, ratio);
        }
    }
}
