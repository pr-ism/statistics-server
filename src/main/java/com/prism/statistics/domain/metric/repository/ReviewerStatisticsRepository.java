package com.prism.statistics.domain.metric.repository;

import com.prism.statistics.domain.metric.repository.dto.ReviewerStatisticsDto;
import java.util.List;

public interface ReviewerStatisticsRepository {

    List<ReviewerStatisticsDto> findReviewerStatisticsByProjectId(Long projectId);
}
