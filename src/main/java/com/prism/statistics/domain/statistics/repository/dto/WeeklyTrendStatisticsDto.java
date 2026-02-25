package com.prism.statistics.domain.statistics.repository.dto;

import java.time.LocalDate;
import java.util.List;

public record WeeklyTrendStatisticsDto(
        List<WeeklyThroughputDto> weeklyThroughputs,
        List<MonthlyThroughputDto> monthlyThroughputs,
        List<WeeklyReviewWaitTimeDto> weeklyReviewWaitTimes,
        List<WeeklyPrSizeDto> weeklyPrSizes
) {

    public record WeeklyThroughputDto(
            LocalDate weekStartDate,
            long mergedCount,
            long closedCount
    ) {}

    public record MonthlyThroughputDto(
            int year,
            int month,
            long mergedCount,
            long closedCount
    ) {}

    public record WeeklyReviewWaitTimeDto(
            LocalDate weekStartDate,
            double avgReviewWaitTimeMinutes
    ) {}

    public record WeeklyPrSizeDto(
            LocalDate weekStartDate,
            double avgSizeScore
    ) {}
}
