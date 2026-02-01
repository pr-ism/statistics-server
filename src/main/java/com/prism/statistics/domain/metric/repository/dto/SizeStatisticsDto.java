package com.prism.statistics.domain.metric.repository.dto;

public record SizeStatisticsDto(
        String sizeCategory,
        long count,
        double averageChangedFileCount,
        double averageCommitCount
) {
}
