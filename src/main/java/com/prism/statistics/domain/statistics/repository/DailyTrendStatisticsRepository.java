package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyTrendStatisticsRepository {

    Optional<DailyTrendStatisticsDto> findDailyTrendStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
