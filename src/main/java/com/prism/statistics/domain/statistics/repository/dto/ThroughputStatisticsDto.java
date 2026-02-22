package com.prism.statistics.domain.statistics.repository.dto;

public record ThroughputStatisticsDto(
        long mergedCount,
        long closedCount,
        long totalMergeTimeMinutes
) {
}
