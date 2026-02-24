package com.prism.statistics.application.statistics.dto.response;

public record ReviewSpeedStatisticsResponse(
        long totalPullRequestCount,
        long reviewedPullRequestCount,
        double reviewRate,
        ReviewWaitTimeStatistics reviewWaitTime,
        MergeWaitTimeStatistics mergeWaitTime,
        ReviewCompletionStatistics reviewCompletion
) {

    private static final long ZERO_COUNT = 0L;
    private static final double ZERO_RATE = 0.0d;
    private static final double ROUNDING_SCALE = 100.0d;

    public static ReviewSpeedStatisticsResponse empty() {
        return new ReviewSpeedStatisticsResponse(
                ZERO_COUNT,
                ZERO_COUNT,
                ZERO_RATE,
                ReviewWaitTimeStatistics.empty(),
                MergeWaitTimeStatistics.empty(),
                ReviewCompletionStatistics.empty()
        );
    }

    public record ReviewWaitTimeStatistics(
            double avgReviewWaitMinutes,
            double reviewWaitP50Minutes,
            double reviewWaitP90Minutes
    ) {
        public static ReviewWaitTimeStatistics empty() {
            return new ReviewWaitTimeStatistics(ZERO_RATE, ZERO_RATE, ZERO_RATE);
        }

        public static ReviewWaitTimeStatistics of(
                double avgReviewWaitMinutes,
                double reviewWaitP50Minutes,
                double reviewWaitP90Minutes
        ) {
            return new ReviewWaitTimeStatistics(
                    roundToTwoDecimals(avgReviewWaitMinutes),
                    roundToTwoDecimals(reviewWaitP50Minutes),
                    roundToTwoDecimals(reviewWaitP90Minutes)
            );
        }
    }

    public record MergeWaitTimeStatistics(
            double avgMergeWaitMinutes,
            long mergedWithApprovalCount
    ) {
        public static MergeWaitTimeStatistics empty() {
            return new MergeWaitTimeStatistics(ZERO_RATE, ZERO_COUNT);
        }

        public static MergeWaitTimeStatistics of(
                double avgMergeWaitMinutes,
                long mergedWithApprovalCount
        ) {
            return new MergeWaitTimeStatistics(
                    roundToTwoDecimals(avgMergeWaitMinutes),
                    mergedWithApprovalCount
            );
        }
    }

    public record ReviewCompletionStatistics(
            double coreTimeReviewRate,
            long coreTimeReviewCount,
            double sameDayReviewRate,
            long sameDayReviewCount
    ) {
        public static ReviewCompletionStatistics empty() {
            return new ReviewCompletionStatistics(ZERO_RATE, ZERO_COUNT, ZERO_RATE, ZERO_COUNT);
        }

        public static ReviewCompletionStatistics of(
                double coreTimeReviewRate,
                long coreTimeReviewCount,
                double sameDayReviewRate,
                long sameDayReviewCount
        ) {
            return new ReviewCompletionStatistics(
                    roundToTwoDecimals(coreTimeReviewRate),
                    coreTimeReviewCount,
                    roundToTwoDecimals(sameDayReviewRate),
                    sameDayReviewCount
            );
        }
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * ROUNDING_SCALE) / ROUNDING_SCALE;
    }
}
