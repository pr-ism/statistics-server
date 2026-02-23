package com.prism.statistics.application.pullrequest;

import com.prism.statistics.application.pullrequest.dto.response.PullRequestDetailResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestListResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestListResponse.PullRequestSummary;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.project.exception.ProjectNotFoundException;
import com.prism.statistics.domain.analysis.metadata.pullrequest.exception.PullRequestNotFoundException;
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
    public PullRequestListResponse findAll(Long userId, Long projectId) {
        validateProjectOwnership(projectId, userId);

        List<PullRequestSummary> pullRequests = pullRequestRepository.findAllByProjectId(projectId).stream()
                .map(pr -> PullRequestSummary.from(pr))
                .toList();

        return new PullRequestListResponse(pullRequests);
    }

    @Transactional(readOnly = true)
    public PullRequestDetailResponse find(Long userId, Long projectId, int pullRequestNumber) {
        validateProjectOwnership(projectId, userId);

        PullRequest pullRequest = pullRequestRepository.findPullRequest(projectId, pullRequestNumber)
                .orElseThrow(() -> new PullRequestNotFoundException());

        return PullRequestDetailResponse.from(pullRequest);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectNotFoundException();
        }
    }
}
