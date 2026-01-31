package com.prism.statistics.domain.metric.repository;

import com.prism.statistics.domain.metric.repository.dto.AuthorStatisticsDto;
import java.util.List;

public interface AuthorStatisticsRepository {

    List<AuthorStatisticsDto> findAuthorStatisticsByProjectId(Long projectId);
}
