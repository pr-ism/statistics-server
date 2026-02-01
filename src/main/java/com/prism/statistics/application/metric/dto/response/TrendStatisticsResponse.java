package com.prism.statistics.application.metric.dto.response;

import java.time.LocalDate;
import java.util.List;

public record TrendStatisticsResponse(
        String period,
        List<TrendDataPoint> trends
) {

    public record TrendDataPoint(
            LocalDate periodStart,
            long prCount,
            double averageChangeAmount
    ) {

        public static TrendDataPoint empty(LocalDate periodStart) {
            return new TrendDataPoint(periodStart, 0L, 0.0);
        }
    }
}
