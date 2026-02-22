package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.ThroughputStatisticsDto;
import java.time.LocalDate;
import java.util.Optional;

public interface ThroughputStatisticsRepository {

    Optional<ThroughputStatisticsDto> findThroughputStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
