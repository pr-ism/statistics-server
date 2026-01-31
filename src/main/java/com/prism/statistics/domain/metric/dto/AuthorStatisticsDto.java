package com.prism.statistics.domain.metric.dto;

public record AuthorStatisticsDto(
        String authorGithubId,
        long pullRequestCount,
        long totalAdditions,
        long totalDeletions,
        double averageAdditions,
        double averageDeletions,
        double averageCommitCount,
        double averageChangedFileCount
) {
}
