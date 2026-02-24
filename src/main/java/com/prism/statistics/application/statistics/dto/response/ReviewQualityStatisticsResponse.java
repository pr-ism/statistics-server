package com.prism.statistics.application.statistics.dto.response;

public record ReviewQualityStatisticsResponse(
        long totalPullRequestCount,
        long reviewedPullRequestCount,
        double reviewRate,
        ReviewActivityStatistics reviewActivity,
        ReviewerStatistics reviewerStats
) {

    private static final long ZERO_COUNT = 0L;
    private static final double ZERO_RATE = 0.0d;
    private static final double ROUNDING_SCALE = 100.0d;

    public static ReviewQualityStatisticsResponse empty() {
        return new ReviewQualityStatisticsResponse(
                ZERO_COUNT,
                ZERO_COUNT,
                ZERO_RATE,
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
            return new ReviewActivityStatistics(
                    ZERO_RATE,
                    ZERO_RATE,
                    ZERO_RATE,
                    ZERO_COUNT,
                    ZERO_COUNT,
                    ZERO_RATE,
                    ZERO_RATE,
                    ZERO_RATE,
                    ZERO_RATE,
                    ZERO_RATE
            );
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
    }

    public record ReviewerStatistics(
            long totalReviewerCount,
            double avgReviewersPerPr,
            double avgSessionDurationMinutes,
            double avgReviewsPerSession
    ) {
        public static ReviewerStatistics empty() {
            return new ReviewerStatistics(ZERO_COUNT, ZERO_RATE, ZERO_RATE, ZERO_RATE);
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
    }

    private static double roundToTwoDecimals(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return ZERO_RATE;
        }

        return Math.round(value * ROUNDING_SCALE) / ROUNDING_SCALE;
    }
}
