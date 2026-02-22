package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.StatisticsSummaryDto;
import java.time.LocalDate;
import java.util.Optional;

public interface StatisticsSummaryRepository {

    Optional<StatisticsSummaryDto> findStatisticsSummaryByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
