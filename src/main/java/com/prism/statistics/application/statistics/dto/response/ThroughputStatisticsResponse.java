package com.prism.statistics.application.statistics.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
