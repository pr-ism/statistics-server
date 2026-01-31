package com.prism.statistics.domain.metric.repository;

import com.prism.statistics.domain.metric.repository.dto.LabelStatisticsDto;
import java.util.List;

public interface LabelStatisticsRepository {

    List<LabelStatisticsDto> findLabelStatisticsByProjectId(Long projectId);
}
