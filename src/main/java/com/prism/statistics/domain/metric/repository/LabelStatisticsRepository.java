package com.prism.statistics.domain.metric.repository;

import com.prism.statistics.domain.metric.repository.dto.LabelStatisticsDto;
import java.time.LocalDate;
import java.util.List;

public interface LabelStatisticsRepository {

    List<LabelStatisticsDto> findLabelStatisticsByProjectId(Long projectId, LocalDate startDate, LocalDate endDate);
}
