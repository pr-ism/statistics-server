package com.prism.statistics.domain.statistics.repository.dto;

public record LifecycleStatisticsDto(
        long totalCount,
        long mergedCount,
        long closedWithoutMergeCount,
        long closedWithoutReviewCount,
        long reopenedCount,
        long totalStateChangeCount,
        long totalTimeToMergeMinutes,
        long totalLifespanMinutes,
        long totalActiveWorkMinutes,
        long activeWorkCount
) {
}
