package com.prism.statistics.domain.metric.repository;

import com.prism.statistics.domain.metric.repository.dto.SizeStatisticsDto;
import java.time.LocalDate;
import java.util.List;

public interface SizeStatisticsRepository {

    List<SizeStatisticsDto> findSizeStatisticsByProjectId(Long projectId, LocalDate startDate, LocalDate endDate);
}
