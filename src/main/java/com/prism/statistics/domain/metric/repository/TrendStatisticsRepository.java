package com.prism.statistics.domain.metric.repository;

import com.prism.statistics.domain.metric.repository.dto.TrendStatisticsDto;
import java.time.LocalDate;
import java.util.List;

public interface TrendStatisticsRepository {

    List<TrendStatisticsDto> findPullRequestsByProjectId(Long projectId, LocalDate startDate, LocalDate endDate);
}
