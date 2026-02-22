package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.PrSizeStatisticsDto;
import java.time.LocalDate;
import java.util.Optional;

public interface PrSizeStatisticsRepository {

    Optional<PrSizeStatisticsDto> findPrSizeStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
