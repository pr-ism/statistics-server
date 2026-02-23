package com.prism.statistics.application.statistics.dto.response;

public record StatisticsSummaryResponse(
        OverviewSummary overview,
        ReviewHealthSummary reviewHealth,
        TeamActivitySummary teamActivity,
        BottleneckSummary bottleneck
) {

    public static StatisticsSummaryResponse empty() {
        return new StatisticsSummaryResponse(
                OverviewSummary.empty(),
                ReviewHealthSummary.empty(),
                TeamActivitySummary.empty(),
                BottleneckSummary.empty()
        );
    }

    public record OverviewSummary(
            long totalPrCount,
            long mergedPrCount,
            long closedPrCount,
            double mergeSuccessRate,
            double avgMergeTimeMinutes,
            double avgSizeScore,
            String dominantSizeGrade
    ) {
        public static OverviewSummary empty() {
            return new OverviewSummary(0L, 0L, 0L, 0.0, 0.0, 0.0, "N/A");
        }

        public static OverviewSummary of(
                long totalPrCount,
                long mergedPrCount,
                long closedPrCount,
                double mergeSuccessRate,
                double avgMergeTimeMinutes,
                double avgSizeScore,
                String dominantSizeGrade
        ) {
            return new OverviewSummary(
                    totalPrCount,
                    mergedPrCount,
                    closedPrCount,
                    roundToTwoDecimals(mergeSuccessRate),
                    roundToTwoDecimals(avgMergeTimeMinutes),
                    roundToTwoDecimals(avgSizeScore),
                    dominantSizeGrade
            );
        }

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    public record ReviewHealthSummary(
            double reviewRate,
            double avgReviewWaitMinutes,
            double firstReviewApproveRate,
            double changesRequestedRate,
            double closedWithoutReviewRate
    ) {
        public static ReviewHealthSummary empty() {
            return new ReviewHealthSummary(0.0, 0.0, 0.0, 0.0, 0.0);
        }

        public static ReviewHealthSummary of(
                double reviewRate,
                double avgReviewWaitMinutes,
                double firstReviewApproveRate,
                double changesRequestedRate,
                double closedWithoutReviewRate
        ) {
            return new ReviewHealthSummary(
                    roundToTwoDecimals(reviewRate),
                    roundToTwoDecimals(avgReviewWaitMinutes),
                    roundToTwoDecimals(firstReviewApproveRate),
                    roundToTwoDecimals(changesRequestedRate),
                    roundToTwoDecimals(closedWithoutReviewRate)
            );
        }

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    public record TeamActivitySummary(
            long totalReviewerCount,
            double avgReviewersPerPr,
            double avgReviewRoundTrips,
            double avgCommentCount,
            double reviewerGiniCoefficient
    ) {
        public static TeamActivitySummary empty() {
            return new TeamActivitySummary(0L, 0.0, 0.0, 0.0, 0.0);
        }

        public static TeamActivitySummary of(
                long totalReviewerCount,
                double avgReviewersPerPr,
                double avgReviewRoundTrips,
                double avgCommentCount,
                double reviewerGiniCoefficient
        ) {
            return new TeamActivitySummary(
                    totalReviewerCount,
                    roundToTwoDecimals(avgReviewersPerPr),
                    roundToTwoDecimals(avgReviewRoundTrips),
                    roundToTwoDecimals(avgCommentCount),
                    roundToTwoDecimals(reviewerGiniCoefficient)
            );
        }

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    public record BottleneckSummary(
            double avgReviewWaitMinutes,
            double avgReviewProgressMinutes,
            double avgMergeWaitMinutes,
            double totalCycleTimeMinutes
    ) {
        public static BottleneckSummary empty() {
            return new BottleneckSummary(0.0, 0.0, 0.0, 0.0);
        }

        public static BottleneckSummary of(
                double avgReviewWaitMinutes,
                double avgReviewProgressMinutes,
                double avgMergeWaitMinutes
        ) {
            double totalCycleTime = avgReviewWaitMinutes + avgReviewProgressMinutes + avgMergeWaitMinutes;
            return new BottleneckSummary(
                    roundToTwoDecimals(avgReviewWaitMinutes),
                    roundToTwoDecimals(avgReviewProgressMinutes),
                    roundToTwoDecimals(avgMergeWaitMinutes),
                    roundToTwoDecimals(totalCycleTime)
            );
        }

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }
}
