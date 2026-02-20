package com.prism.statistics.domain.statistics.repository.dto;

import java.math.BigDecimal;

public record ReviewActivityStatisticsDto(
        long totalCount,
        long reviewedCount,
        long totalReviewRoundTrips,
        long totalCommentCount,
        BigDecimal totalCommentDensity,
        long withAdditionalReviewersCount,
        long withChangesAfterReviewCount
) {
}
