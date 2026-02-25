package com.prism.statistics.application.statistics.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public record CollaborationStatisticsResponse(
        long totalPullRequestCount,
        long reviewedPullRequestCount,
        ReviewerConcentrationStatistics reviewerConcentration,
        DraftPrStatistics draftPr,
        ReviewerAdditionStatistics reviewerAddition,
        List<AuthorReviewWaitTime> authorReviewWaitTimes,
        List<ReviewerStats> reviewerStats
) {

    private static final double ROUND_SCALE = 100.0;

    public static CollaborationStatisticsResponse empty() {
        return new CollaborationStatisticsResponse(
                0L,
                0L,
                ReviewerConcentrationStatistics.empty(),
                DraftPrStatistics.empty(),
                ReviewerAdditionStatistics.empty(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    public record ReviewerConcentrationStatistics(
            double giniCoefficient,
            double top3ReviewerRate,
            long totalReviewerCount
    ) {
        public static ReviewerConcentrationStatistics empty() {
            return new ReviewerConcentrationStatistics(0.0, 0.0, 0L);
        }

        public static ReviewerConcentrationStatistics of(
                double giniCoefficient,
                double top3ReviewerRate,
                long totalReviewerCount
        ) {
            return new ReviewerConcentrationStatistics(
                    roundToTwoDecimals(giniCoefficient),
                    roundToTwoDecimals(top3ReviewerRate),
                    totalReviewerCount
            );
        }
    }

    public record DraftPrStatistics(
            double repeatedDraftPrRate,
            long repeatedDraftPrCount
    ) {
        public static DraftPrStatistics empty() {
            return new DraftPrStatistics(0.0, 0L);
        }

        public static DraftPrStatistics of(double repeatedDraftPrRate, long repeatedDraftPrCount) {
            return new DraftPrStatistics(
                    roundToTwoDecimals(repeatedDraftPrRate),
                    repeatedDraftPrCount
            );
        }
    }

    public record ReviewerAdditionStatistics(
            double reviewerAddedRate,
            long reviewerAddedPrCount
    ) {
        public static ReviewerAdditionStatistics empty() {
            return new ReviewerAdditionStatistics(0.0, 0L);
        }

        public static ReviewerAdditionStatistics of(double reviewerAddedRate, long reviewerAddedPrCount) {
            return new ReviewerAdditionStatistics(
                    roundToTwoDecimals(reviewerAddedRate),
                    reviewerAddedPrCount
            );
        }
    }

    public record AuthorReviewWaitTime(
            Long authorId,
            String authorName,
            double avgReviewWaitMinutes,
            long prCount
    ) {
        public static AuthorReviewWaitTime of(
                Long authorId,
                String authorName,
                double avgReviewWaitMinutes,
                long prCount
        ) {
            return new AuthorReviewWaitTime(
                    authorId,
                    authorName,
                    roundToTwoDecimals(avgReviewWaitMinutes),
                    prCount
            );
        }
    }

    public record ReviewerStats(
            Long reviewerId,
            String reviewerName,
            long reviewCount,
            double avgResponseTimeMinutes
    ) {
        public static ReviewerStats of(
                Long reviewerId,
                String reviewerName,
                long reviewCount,
                double avgResponseTimeMinutes
        ) {
            return new ReviewerStats(
                    reviewerId,
                    reviewerName,
                    reviewCount,
                    roundToTwoDecimals(avgResponseTimeMinutes)
            );
        }
    }

    private static double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
