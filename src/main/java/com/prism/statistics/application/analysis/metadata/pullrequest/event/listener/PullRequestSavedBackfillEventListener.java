package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSavedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.CommitRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestContentHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestSavedBackfillEventListener {

    private final CommitRepository commitRepository;
    private final PullRequestFileRepository pullRequestFileRepository;
    private final PullRequestContentHistoryRepository pullRequestContentHistoryRepository;
    private final PullRequestFileHistoryRepository pullRequestFileHistoryRepository;

    @Async("backfillExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillCommit(PullRequestSavedEvent event) {
        commitRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }

    @Async("backfillExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillPullRequestFile(PullRequestSavedEvent event) {
        pullRequestFileRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }

    @Async("backfillExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillPullRequestContentHistory(PullRequestSavedEvent event) {
        pullRequestContentHistoryRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }

    @Async("backfillExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillPullRequestFileHistory(PullRequestSavedEvent event) {
        pullRequestFileHistoryRepository.backfillPullRequestId(event.githubPullRequestId(), event.pullRequestId());
    }
}
