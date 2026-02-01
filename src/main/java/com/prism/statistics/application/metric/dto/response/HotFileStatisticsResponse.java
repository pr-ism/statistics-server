package com.prism.statistics.application.metric.dto.response;

import com.prism.statistics.domain.metric.repository.dto.HotFileStatisticsDto;
import java.util.List;

public record HotFileStatisticsResponse(
        List<HotFileStatistics> hotFiles
) {
    public record HotFileStatistics(
            String fileName,
            long changeCount,
            long totalAdditions,
            long totalDeletions,
            long modifiedCount,
            long addedCount,
            long removedCount,
            long renamedCount
    ) {
        public static HotFileStatistics from(HotFileStatisticsDto dto) {
            return new HotFileStatistics(
                    dto.fileName(),
                    dto.changeCount(),
                    dto.totalAdditions(),
                    dto.totalDeletions(),
                    dto.modifiedCount(),
                    dto.addedCount(),
                    dto.removedCount(),
                    dto.renamedCount()
            );
        }
    }
}
