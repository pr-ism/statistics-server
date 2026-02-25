package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto;

import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyTrendStatisticsRepository {

    Optional<WeeklyTrendStatisticsDto> findWeeklyTrendStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
