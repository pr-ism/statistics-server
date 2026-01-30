package com.prism.statistics.application.webhook;

import com.prism.statistics.application.webhook.dto.request.ReviewerRemovedRequest;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.reviewer.RequestedReviewerChangeHistory;
import com.prism.statistics.domain.reviewer.enums.ReviewerAction;
import com.prism.statistics.domain.reviewer.repository.RequestedReviewerChangeHistoryRepository;
import com.prism.statistics.domain.reviewer.repository.RequestedReviewerRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewerRemovedService {

    private final Clock clock;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final RequestedReviewerRepository requestedReviewerRepository;
    private final RequestedReviewerChangeHistoryRepository requestedReviewerChangeHistoryRepository;

    @Transactional
    public void removeReviewer(String apiKey, ReviewerRemovedRequest request) {
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new ProjectNotFoundException());

        PullRequest pullRequest = pullRequestRepository.findWithLock(projectId, request.prNumber())
                .orElseThrow(() -> new PullRequestNotFoundException());

        Long pullRequestId = pullRequest.getId();
        Long githubUid = request.reviewer().id();

        long deleted = requestedReviewerRepository.delete(pullRequestId, githubUid);

        if (deleted == 0L) {
            return;
        }

        String githubMention = request.reviewer().login();
        LocalDateTime removedAt = toLocalDateTime(request.removedAt());

        RequestedReviewerChangeHistory history = RequestedReviewerChangeHistory.create(
                pullRequestId,
                githubMention,
                githubUid,
                ReviewerAction.REMOVED,
                removedAt
        );

        requestedReviewerChangeHistoryRepository.save(history);
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }
}
