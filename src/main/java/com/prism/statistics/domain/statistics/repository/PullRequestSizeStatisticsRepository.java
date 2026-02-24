package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.PullRequestSizeStatisticsDto;

import java.time.LocalDate;
import java.util.Optional;

public interface PullRequestSizeStatisticsRepository {

    Optional<PullRequestSizeStatisticsDto> findPullRequestSizeStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
