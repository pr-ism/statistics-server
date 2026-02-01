package com.prism.statistics.application.metric;

import com.prism.statistics.application.metric.dto.request.LabelStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse.LabelStatistics;
import com.prism.statistics.domain.metric.repository.LabelStatisticsRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LabelStatisticsQueryService {

    private final LabelStatisticsRepository labelStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public LabelStatisticsResponse findLabelStatistics(Long userId, Long projectId, LabelStatisticsRequest request) {
        validateProjectOwnership(projectId, userId);

        List<LabelStatistics> labelStatistics = labelStatisticsRepository
                .findLabelStatisticsByProjectId(projectId, request.startDate(), request.endDate())
                .stream()
                .map(labelStat -> LabelStatistics.from(labelStat))
                .toList();

        return new LabelStatisticsResponse(labelStatistics);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
