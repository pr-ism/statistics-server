package com.prism.statistics.domain.statistics.repository.dto;

import java.time.LocalDate;
import java.util.List;

public record DailyTrendStatisticsDto(
        List<DailyPrCountDto> dailyCreatedCounts,
        List<DailyPrCountDto> dailyMergedCounts
) {

    public record DailyPrCountDto(
            LocalDate date,
            long count
    ) {
    }
}
