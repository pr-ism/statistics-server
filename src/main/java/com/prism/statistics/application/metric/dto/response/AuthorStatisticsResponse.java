package com.prism.statistics.application.metric.dto.response;

import com.prism.statistics.domain.metric.dto.AuthorStatisticsDto;
import java.util.List;

public record AuthorStatisticsResponse(
        List<AuthorStatistics> authorStatistics
) {
    public record AuthorStatistics(
            String authorGithubId,
            long pullRequestCount,
            long totalAdditions,
            long totalDeletions,
            double averageAdditions,
            double averageDeletions,
            double averageCommitCount,
            double averageChangedFileCount
    ) {
        public static AuthorStatistics from(AuthorStatisticsDto dto) {
            return new AuthorStatistics(
                    dto.authorGithubId(),
                    dto.pullRequestCount(),
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
