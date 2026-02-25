package com.prism.statistics.application.statistics.dto.response;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record DailyTrendStatisticsResponse(
        List<DailyPrTrend> dailyCreatedTrend,
        List<DailyPrTrend> dailyMergedTrend,
        TrendSummary summary
) {

    private static final double ROUND_SCALE = 100.0;
    private static final long ZERO_LONG = 0L;
    private static final double ZERO_DOUBLE = 0.0;

    public static DailyTrendStatisticsResponse empty() {
        return new DailyTrendStatisticsResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                TrendSummary.empty()
        );
    }

    public record DailyPrTrend(
            LocalDate date,
            long count
    ) {
        public static DailyPrTrend of(LocalDate date, long count) {
            return new DailyPrTrend(date, count);
        }
    }

    public record TrendSummary(
            long totalCreatedCount,
            long totalMergedCount,
            double avgDailyCreatedCount,
            double avgDailyMergedCount,
            LocalDate peakCreatedDate,
            long peakCreatedCount,
            LocalDate peakMergedDate,
            long peakMergedCount
    ) {
        public static TrendSummary empty() {
            return new TrendSummary(ZERO_LONG, ZERO_LONG, ZERO_DOUBLE, ZERO_DOUBLE, null, ZERO_LONG, null, ZERO_LONG);
        }

        public static TrendSummary of(
                long totalCreatedCount,
                long totalMergedCount,
                double avgDailyCreatedCount,
                double avgDailyMergedCount,
                LocalDate peakCreatedDate,
                long peakCreatedCount,
                LocalDate peakMergedDate,
                long peakMergedCount
        ) {
            return new TrendSummary(
                    totalCreatedCount,
                    totalMergedCount,
                    roundToTwoDecimals(avgDailyCreatedCount),
                    roundToTwoDecimals(avgDailyMergedCount),
                    peakCreatedDate,
                    peakCreatedCount,
                    peakMergedDate,
                    peakMergedCount
            );
        }

        private static double roundToTwoDecimals(double value) {
            return BigDecimal.valueOf(value)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }
    }
}
