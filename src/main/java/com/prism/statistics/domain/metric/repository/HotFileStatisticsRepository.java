package com.prism.statistics.domain.metric.repository;

import com.prism.statistics.domain.metric.repository.dto.HotFileStatisticsDto;
import java.time.LocalDate;
import java.util.List;

public interface HotFileStatisticsRepository {

    List<HotFileStatisticsDto> findHotFileStatisticsByProjectId(Long projectId, LocalDate startDate, LocalDate endDate, int limit);
}
