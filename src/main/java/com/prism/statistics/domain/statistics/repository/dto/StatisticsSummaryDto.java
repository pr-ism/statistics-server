package com.prism.statistics.domain.statistics.repository.dto;

import java.math.BigDecimal;

public record StatisticsSummaryDto(
        OverviewDto overview,
        ReviewHealthDto reviewHealth,
        TeamActivityDto teamActivity,
        BottleneckDto bottleneck
) {

    public record OverviewDto(
            long totalPrCount,
            long mergedPrCount,
            long closedPrCount,
            long totalMergeTimeMinutes,
            BigDecimal totalSizeScore,
            long sizeMeasuredCount,
            String dominantSizeGrade
    ) {
    }

    public record ReviewHealthDto(
            long totalPrCount,
            long reviewedPrCount,
            long totalReviewWaitMinutes,
            long firstReviewApproveCount,
            long changesRequestedCount,
            long closedWithoutReviewCount
    ) {
    }

    public record TeamActivityDto(
            long uniqueReviewerCount,
            long uniquePullRequestCount,
            long totalReviewerAssignments,
            long totalReviewRoundTrips,
            long totalCommentCount,
            double reviewerGiniCoefficient
    ) {
    }

    public record BottleneckDto(
            long totalReviewWaitMinutes,
            long totalReviewProgressMinutes,
            long totalMergeWaitMinutes,
            long bottleneckCount
    ) {
    }
}
