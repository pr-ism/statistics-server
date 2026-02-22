package com.prism.statistics.domain.statistics.repository;

import com.prism.statistics.domain.statistics.repository.dto.ReviewSpeedStatisticsDto;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public interface ReviewSpeedStatisticsRepository {

    Optional<ReviewSpeedStatisticsDto> findReviewSpeedStatisticsByProjectId(
            Long projectId,
            LocalDate startDate,
            LocalDate endDate,
            LocalTime coreTimeStart,
            LocalTime coreTimeEnd
    );
}
