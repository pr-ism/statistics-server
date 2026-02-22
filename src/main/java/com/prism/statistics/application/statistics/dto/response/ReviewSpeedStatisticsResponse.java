package com.prism.statistics.application.statistics.dto.response;

public record ReviewSpeedStatisticsResponse(
        long totalPullRequestCount,
        long reviewedPullRequestCount,
        double reviewRate,
        ReviewWaitTimeStatistics reviewWaitTime,
        MergeWaitTimeStatistics mergeWaitTime,
        ReviewCompletionStatistics reviewCompletion
) {

    public static ReviewSpeedStatisticsResponse empty() {
        return new ReviewSpeedStatisticsResponse(
                0L,
                0L,
                0.0,
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
            return new ReviewWaitTimeStatistics(0.0, 0.0, 0.0);
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

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    public record MergeWaitTimeStatistics(
            double avgMergeWaitMinutes,
            long mergedWithApprovalCount
    ) {
        public static MergeWaitTimeStatistics empty() {
            return new MergeWaitTimeStatistics(0.0, 0L);
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

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    public record ReviewCompletionStatistics(
            double coreTimeReviewRate,
            long coreTimeReviewCount,
            double sameDayReviewRate,
            long sameDayReviewCount
    ) {
        public static ReviewCompletionStatistics empty() {
            return new ReviewCompletionStatistics(0.0, 0L, 0.0, 0L);
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

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }
}
