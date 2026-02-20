package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReadyForReviewRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestStateChangedEvent;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestReadyForReviewService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void readyForReview(String apiKey, PullRequestReadyForReviewRequest request) {
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());

        pullRequestRepository.findPullRequest(projectId, request.pullRequestNumber())
                .ifPresentOrElse(
                        pullRequest -> processReadyForReview(pullRequest, request),
                        () -> log.warn("PullRequest를 찾을 수 없습니다. pullRequestNumber={}", request.pullRequestNumber())
                );
    }

    private void processReadyForReview(PullRequest pullRequest, PullRequestReadyForReviewRequest request) {
        if (!pullRequest.isDraft()) {
            log.info("DRAFT 상태가 아닌 PullRequest입니다. pullRequestNumber={}, state={}", request.pullRequestNumber(), pullRequest.getState());
            return;
        }

        PullRequestState previousState = pullRequest.getState();
        LocalDateTime readyForReviewAt = localDateTimeConverter.toLocalDateTime(request.readyForReviewAt());
        pullRequest.changeStateToReadyForReview();
        publishStateChangedEvent(pullRequest, previousState, readyForReviewAt);
    }

    private void publishStateChangedEvent(PullRequest pullRequest, PullRequestState previousState, LocalDateTime githubChangedAt) {
        eventPublisher.publishEvent(new PullRequestStateChangedEvent(
                pullRequest.getId(),
                pullRequest.getHeadCommitSha(),
                previousState,
                pullRequest.getState(),
                githubChangedAt
        ));
    }
}
