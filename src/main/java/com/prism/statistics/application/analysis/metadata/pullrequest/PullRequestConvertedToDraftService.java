package com.prism.statistics.application.analysis.metadata.pullrequest;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestConvertedToDraftRequest;
import com.prism.statistics.application.collect.inbox.aop.InboxEnqueue;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestStateChangedEvent;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestConvertedToDraftService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final PullRequestRepository pullRequestRepository;
    private final ApplicationEventPublisher eventPublisher;

    @InboxEnqueue(CollectInboxType.PULL_REQUEST_CONVERTED_TO_DRAFT)
    @Transactional
    public void convertToDraft(Long projectId, PullRequestConvertedToDraftRequest request) {
        pullRequestRepository.findPullRequest(projectId, request.pullRequestNumber())
                .ifPresentOrElse(
                        pullRequest -> processConvertedToDraft(pullRequest, request),
                        () -> log.warn("PullRequest를 찾을 수 없습니다. pullRequestNumber={}", request.pullRequestNumber())
                );
    }

    private void processConvertedToDraft(PullRequest pullRequest, PullRequestConvertedToDraftRequest request) {
        if (!pullRequest.isOpen()) {
            log.info("OPEN 상태가 아닌 PullRequest입니다. pullRequestNumber={}, state={}", request.pullRequestNumber(), pullRequest.getState());
            return;
        }

        PullRequestState previousState = pullRequest.getState();
        LocalDateTime convertedToDraftAt = localDateTimeConverter.toLocalDateTime(request.convertedToDraftAt());
        pullRequest.changeStateToDraft();
        publishStateChangedEvent(pullRequest, previousState, convertedToDraftAt);
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
