package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.LifecycleStatisticsDto;
import java.time.LocalDate;
import java.util.Optional;

public interface LifecycleStatisticsRepository {

    Optional<LifecycleStatisticsDto> findLifecycleStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
