package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto;
import java.time.LocalDate;
import java.util.Optional;

public interface CollaborationStatisticsRepository {

    Optional<CollaborationStatisticsDto> findCollaborationStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
