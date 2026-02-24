package com.prism.statistics.application.statistics.dto.response;

public record ThroughputStatisticsResponse(
        long mergedPrCount,
        long closedPrCount,
        double avgMergeTimeMinutes,
        double mergeSuccessRate,
        double closedPrRate
) {

    private static final double ROUND_SCALE = 100.0;
    private static final long ZERO_LONG = 0L;
    private static final double ZERO_DOUBLE = 0.0;

    public static ThroughputStatisticsResponse empty() {
        return new ThroughputStatisticsResponse(ZERO_LONG, ZERO_LONG, ZERO_DOUBLE, ZERO_DOUBLE, ZERO_DOUBLE);
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
        return Math.round(value * ROUND_SCALE) / ROUND_SCALE;
    }
}
