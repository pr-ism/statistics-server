package com.prism.statistics.application.statistics.dto.response;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record DailyTrendStatisticsResponse(
        List<DailyPrTrend> dailyCreatedTrend,
        List<DailyPrTrend> dailyMergedTrend,
        TrendSummary summary
) {

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
            return new TrendSummary(0L, 0L, 0.0, 0.0, null, 0L, null, 0L);
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
            return Math.round(value * 100.0) / 100.0;
        }
    }
}
