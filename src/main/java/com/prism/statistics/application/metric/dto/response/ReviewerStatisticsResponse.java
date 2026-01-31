package com.prism.statistics.application.metric.dto.response;

import com.prism.statistics.domain.metric.repository.dto.ReviewerStatisticsDto;
import java.util.List;

public record ReviewerStatisticsResponse(
        List<ReviewerStatistics> reviewerStatistics
) {
    public record ReviewerStatistics(
            String reviewerGithubMention,
            long reviewCount,
            long totalAdditions,
            long totalDeletions,
            double averageAdditions,
            double averageDeletions,
            double averageCommitCount,
            double averageChangedFileCount
    ) {
        public static ReviewerStatistics from(ReviewerStatisticsDto dto) {
            return new ReviewerStatistics(
                    dto.reviewerGithubMention(),
                    dto.reviewCount(),
                    dto.totalAdditions(),
                    dto.totalDeletions(),
                    dto.averageAdditions(),
                    dto.averageDeletions(),
                    dto.averageCommitCount(),
                    dto.averageChangedFileCount()
            );
        }
    }
}
