package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestStateChangedEvent;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestClosedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final PullRequestRepository pullRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void closePullRequest(String apiKey, PullRequestClosedRequest request) {
        Long projectId = projectRepository.findIdByApiKey(apiKey)
                .orElseThrow(() -> new InvalidApiKeyException());

        pullRequestRepository.findPullRequest(projectId, request.pullRequestNumber())
                .ifPresentOrElse(
                        pullRequest -> processClosed(pullRequest, request),
                        () -> log.warn("PullRequest를 찾을 수 없습니다. pullRequestNumber={}", request.pullRequestNumber())
                );
    }

    private void processClosed(PullRequest pullRequest, PullRequestClosedRequest request) {
        if (pullRequest.isClosed() || pullRequest.isMerged()) {
            log.info("이미 닫힌 PullRequest입니다. pullRequestNumber={}", request.pullRequestNumber());
            return;
        }

        PullRequestState previousState = pullRequest.getState();
        LocalDateTime githubChangedAt = changeState(pullRequest, request);
        publishStateChangedEvent(pullRequest, previousState, githubChangedAt);
    }

    private LocalDateTime changeState(PullRequest pullRequest, PullRequestClosedRequest request) {
        if (request.isMerged()) {
            LocalDateTime mergedAt = localDateTimeConverter.toLocalDateTime(request.mergedAt());
            pullRequest.changeStateToMerged(mergedAt);
            return mergedAt;
        }

        LocalDateTime closedAt = localDateTimeConverter.toLocalDateTime(request.closedAt());
        pullRequest.changeStateToClosed(closedAt);
        return closedAt;
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
