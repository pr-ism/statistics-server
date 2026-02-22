package com.prism.statistics.application.statistics.dto.response;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record WeeklyTrendStatisticsResponse(
        List<WeeklyThroughput> weeklyThroughput,
        List<MonthlyThroughput> monthlyThroughput,
        List<WeeklyReviewWaitTime> weeklyReviewWaitTimeTrend,
        List<WeeklyPrSize> weeklyPrSizeTrend
) {

    public static WeeklyTrendStatisticsResponse empty() {
        return new WeeklyTrendStatisticsResponse(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    public record WeeklyThroughput(
            LocalDate weekStartDate,
            long mergedCount,
            long closedCount
    ) {
        public static WeeklyThroughput of(LocalDate weekStartDate, long mergedCount, long closedCount) {
            return new WeeklyThroughput(weekStartDate, mergedCount, closedCount);
        }
    }

    public record MonthlyThroughput(
            int year,
            int month,
            long mergedCount,
            long closedCount
    ) {
        public static MonthlyThroughput of(int year, int month, long mergedCount, long closedCount) {
            return new MonthlyThroughput(year, month, mergedCount, closedCount);
        }
    }

    public record WeeklyReviewWaitTime(
            LocalDate weekStartDate,
            double avgReviewWaitTimeMinutes
    ) {
        public static WeeklyReviewWaitTime of(LocalDate weekStartDate, double avgReviewWaitTimeMinutes) {
            return new WeeklyReviewWaitTime(weekStartDate, roundToTwoDecimals(avgReviewWaitTimeMinutes));
        }

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }

    public record WeeklyPrSize(
            LocalDate weekStartDate,
            double avgSizeScore
    ) {
        public static WeeklyPrSize of(LocalDate weekStartDate, double avgSizeScore) {
            return new WeeklyPrSize(weekStartDate, roundToTwoDecimals(avgSizeScore));
        }

        private static double roundToTwoDecimals(double value) {
            return Math.round(value * 100.0) / 100.0;
        }
    }
}
