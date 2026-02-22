package com.prism.statistics.application.statistics.dto.response;

public record ReviewQualityStatisticsResponse(
        long totalPullRequestCount,
        long reviewedPullRequestCount,
        double reviewRate,
        ReviewActivityStatistics reviewActivity,
        ReviewerStatistics reviewerStats
) {

    public static ReviewQualityStatisticsResponse empty() {
        return new ReviewQualityStatisticsResponse(
                0L,
                0L,
                0.0,
                ReviewActivityStatistics.empty(),
                ReviewerStatistics.empty()
        );
    }

    public record ReviewActivityStatistics(
            double avgReviewRoundTrips,
            double avgCommentCount,
            double avgCommentDensity,
            long withAdditionalReviewersCount,
            long withChangesAfterReviewCount,
            double firstReviewApproveRate,
            double postReviewCommitRate,
            double changesRequestedRate,
            double avgChangesResolutionMinutes,
            double highIntensityPrRate
    ) {
        public static ReviewActivityStatistics empty() {
            return new ReviewActivityStatistics(0.0, 0.0, 0.0, 0L, 0L, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        public static ReviewActivityStatistics of(
                double avgReviewRoundTrips,
                double avgCommentCount,
                double avgCommentDensity,
                long withAdditionalReviewersCount,
                long withChangesAfterReviewCount,
                double firstReviewApproveRate,
                double postReviewCommitRate,
                double changesRequestedRate,
                double avgChangesResolutionMinutes,
                double highIntensityPrRate
        ) {
            return new ReviewActivityStatistics(
                    roundToTwoDecimals(avgReviewRoundTrips),
                    roundToTwoDecimals(avgCommentCount),
                    roundToTwoDecimals(avgCommentDensity),
                    withAdditionalReviewersCount,
                    withChangesAfterReviewCount,
                    roundToTwoDecimals(firstReviewApproveRate),
                    roundToTwoDecimals(postReviewCommitRate),
                    roundToTwoDecimals(changesRequestedRate),
                    roundToTwoDecimals(avgChangesResolutionMinutes),
                    roundToTwoDecimals(highIntensityPrRate)
            );
        }

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    public record ReviewerStatistics(
            long totalReviewerCount,
            double avgReviewersPerPr,
            double avgSessionDurationMinutes,
            double avgReviewsPerSession
    ) {
        public static ReviewerStatistics empty() {
            return new ReviewerStatistics(0L, 0.0, 0.0, 0.0);
        }

        public static ReviewerStatistics of(
                long totalReviewerCount,
                double avgReviewersPerPr,
                double avgSessionDurationMinutes,
                double avgReviewsPerSession
        ) {
            return new ReviewerStatistics(
                    totalReviewerCount,
                    roundToTwoDecimals(avgReviewersPerPr),
                    roundToTwoDecimals(avgSessionDurationMinutes),
                    roundToTwoDecimals(avgReviewsPerSession)
            );
        }

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }
}
