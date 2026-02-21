package com.prism.statistics.application.statistics.dto.response;

public record LifecycleStatisticsResponse(
        long totalPullRequestCount,
        long mergedCount,
        long closedWithoutMergeCount,
        double mergeRate,
        AverageTimeStatistics averageTime,
        HealthStatistics health
) {

    public record AverageTimeStatistics(
            long averageTimeToMergeMinutes,
            long averageLifespanMinutes,
            long averageActiveWorkMinutes
    ) {
        public static AverageTimeStatistics empty() {
            return new AverageTimeStatistics(0L, 0L, 0L);
        }

        public static AverageTimeStatistics of(
                long averageTimeToMergeMinutes,
                long averageLifespanMinutes,
                long averageActiveWorkMinutes
        ) {
            return new AverageTimeStatistics(
                    averageTimeToMergeMinutes,
                    averageLifespanMinutes,
                    averageActiveWorkMinutes
            );
        }
    }

    public record HealthStatistics(
            long closedWithoutReviewCount,
            double closedWithoutReviewRate,
            long reopenedCount,
            double reopenedRate,
            double averageStateChangeCount
    ) {
        public static HealthStatistics empty() {
            return new HealthStatistics(0L, 0.0, 0L, 0.0, 0.0);
        }

        public static HealthStatistics of(
                long closedWithoutReviewCount,
                double closedWithoutReviewRate,
                long reopenedCount,
                double reopenedRate,
                double averageStateChangeCount
        ) {
            return new HealthStatistics(
                    closedWithoutReviewCount,
                    closedWithoutReviewRate,
                    reopenedCount,
                    reopenedRate,
                    averageStateChangeCount
            );
        }
    }

    public static LifecycleStatisticsResponse empty() {
        return new LifecycleStatisticsResponse(
                0L, 0L, 0L, 0.0,
                AverageTimeStatistics.empty(),
                HealthStatistics.empty()
        );
    }
}
