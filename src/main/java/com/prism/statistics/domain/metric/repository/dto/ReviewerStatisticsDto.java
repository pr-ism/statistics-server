package com.prism.statistics.domain.metric.repository.dto;

public record ReviewerStatisticsDto(
        String reviewerGithubMention,
        long reviewCount,
        long totalAdditions,
        long totalDeletions,
        double averageAdditions,
        double averageDeletions,
        double averageCommitCount,
        double averageChangedFileCount
) {
}
