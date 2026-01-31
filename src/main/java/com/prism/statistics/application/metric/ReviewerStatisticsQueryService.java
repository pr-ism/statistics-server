package com.prism.statistics.application.metric;

import com.prism.statistics.application.metric.dto.response.ReviewerStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.ReviewerStatisticsResponse.ReviewerStatistics;
import com.prism.statistics.domain.metric.repository.ReviewerStatisticsRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewerStatisticsQueryService {

    private final ReviewerStatisticsRepository reviewerStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public ReviewerStatisticsResponse findReviewerStatistics(Long userId, Long projectId) {
        validateProjectOwnership(projectId, userId);

        List<ReviewerStatistics> reviewerStatistics = reviewerStatisticsRepository
                .findReviewerStatisticsByProjectId(projectId).stream()
                .map(reviewerStat -> ReviewerStatistics.from(reviewerStat))
                .toList();

        return new ReviewerStatisticsResponse(reviewerStatistics);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectNotFoundException();
        }
    }
}
