package com.prism.statistics.domain.statistics.repository.dto;

public record ReviewSessionStatisticsDto(
        long totalSessionCount,
        long uniqueReviewerCount,
        long uniquePullRequestCount,
        long totalSessionDurationMinutes,
        long totalReviewCount
) {
}
