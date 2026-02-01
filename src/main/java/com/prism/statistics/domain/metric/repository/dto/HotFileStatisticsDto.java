package com.prism.statistics.domain.metric.repository.dto;

public record HotFileStatisticsDto(
        String fileName,
        long changeCount,
        long totalAdditions,
        long totalDeletions,
        long modifiedCount,
        long addedCount,
        long removedCount,
        long renamedCount
) {
}
