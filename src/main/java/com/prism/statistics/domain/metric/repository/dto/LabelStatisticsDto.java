package com.prism.statistics.domain.metric.repository.dto;

public record LabelStatisticsDto(
        String labelName,
        long pullRequestCount,
        long totalAdditions,
        long totalDeletions,
        double averageAdditions,
        double averageDeletions,
        double averageCommitCount,
        double averageChangedFileCount
) {
}
