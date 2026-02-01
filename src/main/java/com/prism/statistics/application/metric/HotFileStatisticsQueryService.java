package com.prism.statistics.application.metric;

import com.prism.statistics.application.metric.dto.request.HotFileStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.HotFileStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.HotFileStatisticsResponse.HotFileStatistics;
import com.prism.statistics.domain.metric.repository.HotFileStatisticsRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HotFileStatisticsQueryService {

    private final HotFileStatisticsRepository hotFileStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public HotFileStatisticsResponse findHotFileStatistics(Long userId, Long projectId, HotFileStatisticsRequest request) {
        validateProjectOwnership(projectId, userId);

        List<HotFileStatistics> hotFiles = hotFileStatisticsRepository
                .findHotFileStatisticsByProjectId(projectId, request.startDate(), request.endDate(), request.limitOrDefault())
                .stream()
                .map(hotFile -> HotFileStatistics.from(hotFile))
                .toList();

        return new HotFileStatisticsResponse(hotFiles);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
