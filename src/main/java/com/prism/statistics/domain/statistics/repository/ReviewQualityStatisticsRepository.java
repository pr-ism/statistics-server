package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.ReviewActivityStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSessionStatisticsDto;
import java.time.LocalDate;
import java.util.Optional;

public interface ReviewQualityStatisticsRepository {

    Optional<ReviewActivityStatisticsDto> findReviewActivityStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );

    Optional<ReviewSessionStatisticsDto> findReviewSessionStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate
    );
}
