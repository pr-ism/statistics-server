package com.prism.statistics.application.statistics.dto.response;

public record ThroughputStatisticsResponse(
        long mergedPrCount,
        long closedPrCount,
        double avgMergeTimeMinutes,
        double mergeSuccessRate,
        double closedPrRate
) {

    public static ThroughputStatisticsResponse empty() {
        return new ThroughputStatisticsResponse(0L, 0L, 0.0, 0.0, 0.0);
    }

    public static ThroughputStatisticsResponse of(
            long mergedPrCount,
            long closedPrCount,
            double avgMergeTimeMinutes,
            double mergeSuccessRate,
            double closedPrRate
    ) {
        return new ThroughputStatisticsResponse(
                mergedPrCount,
                closedPrCount,
                roundToTwoDecimals(avgMergeTimeMinutes),
                roundToTwoDecimals(mergeSuccessRate),
                roundToTwoDecimals(closedPrRate)
        );
    }

    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
