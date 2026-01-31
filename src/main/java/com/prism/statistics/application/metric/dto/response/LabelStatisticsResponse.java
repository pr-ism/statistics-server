package com.prism.statistics.application.metric.dto.response;

import com.prism.statistics.domain.metric.repository.dto.LabelStatisticsDto;
import java.util.List;

public record LabelStatisticsResponse(
        List<LabelStatistics> labelStatistics
) {
    public record LabelStatistics(
            String labelName,
            long pullRequestCount,
            long totalAdditions,
            long totalDeletions,
            double averageAdditions,
            double averageDeletions,
            double averageCommitCount,
            double averageChangedFileCount
    ) {
        public static LabelStatistics from(LabelStatisticsDto dto) {
            return new LabelStatistics(
                    dto.labelName(),
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
