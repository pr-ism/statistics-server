package com.prism.statistics.application.pullrequest;

import com.prism.statistics.application.pullrequest.dto.response.PullRequestDetailResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestListResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestListResponse.PullRequestSummary;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PullRequestQueryService {

    private final PullRequestRepository pullRequestRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public PullRequestListResponse findAllByProjectId(Long userId, Long projectId) {
        validateProjectOwnership(projectId, userId);

        List<PullRequestSummary> pullRequests = pullRequestRepository.findAllByProjectId(projectId).stream()
                .map(PullRequestSummary::from)
                .toList();

        return new PullRequestListResponse(pullRequests);
    }

    @Transactional(readOnly = true)
    public PullRequestDetailResponse findByProjectIdAndPrNumber(Long userId, Long projectId, int prNumber) {
        validateProjectOwnership(projectId, userId);

        PullRequest pullRequest = pullRequestRepository.findPullRequest(projectId, prNumber)
                .orElseThrow(PullRequestNotFoundException::new);

        return PullRequestDetailResponse.from(pullRequest);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        projectRepository.existsByIdAndUserId(projectId, userId)
                .orElseThrow(ProjectNotFoundException::new);
    }
}
