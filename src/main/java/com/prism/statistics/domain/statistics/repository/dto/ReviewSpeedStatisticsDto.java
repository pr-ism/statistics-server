package com.prism.statistics.domain.statistics.repository.dto;

import java.util.List;

public record ReviewSpeedStatisticsDto(
        long totalCount,
        long reviewedCount,
        long totalReviewWaitMinutes,
        List<Long> reviewWaitMinutesList,
        long mergedWithApprovalCount,
        long totalMergeWaitMinutes,
        long coreTimeReviewCount,
        long sameDayReviewCount
) {
}
