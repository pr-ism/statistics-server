package com.prism.statistics.application.metric;

import com.prism.statistics.application.metric.dto.response.AuthorStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.AuthorStatisticsResponse.AuthorStatistics;
import com.prism.statistics.domain.metric.repository.AuthorStatisticsRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthorStatisticsQueryService {

    private final AuthorStatisticsRepository authorStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public AuthorStatisticsResponse findAuthorStatistics(Long userId, Long projectId) {
        validateProjectOwnership(projectId, userId);

        List<AuthorStatistics> authorStatistics = authorStatisticsRepository
                .findAuthorStatisticsByProjectId(projectId).stream()
                .map(authorStat -> AuthorStatistics.from(authorStat))
                .toList();

        return new AuthorStatisticsResponse(authorStatistics);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
