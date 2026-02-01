package com.prism.statistics.application.metric.dto.response;

import java.util.List;

public record SizeStatisticsResponse(
        List<SizeStatistics> sizeStatistics
) {
    public record SizeStatistics(
            String sizeCategory,
            long count,
            double percentage,
            double averageChangedFileCount,
            double averageCommitCount
    ) {
        public static SizeStatistics empty(String sizeCategory) {
            return new SizeStatistics(sizeCategory, 0, 0.0, 0.0, 0.0);
        }

        public static SizeStatistics of(
                String sizeCategory,
                long count,
                double percentage,
                double averageChangedFileCount,
                double averageCommitCount
        ) {
            return new SizeStatistics(sizeCategory, count, percentage, averageChangedFileCount, averageCommitCount);
        }
    }
}
