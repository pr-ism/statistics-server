package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSynchronizedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestContentHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestContentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestContentHistoryEventListener {

    private final PullRequestContentHistoryRepository pullRequestContentHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveInitialContentHistory(PullRequestOpenCreatedEvent event) {
        PullRequestContentHistory pullRequestContentHistory = PullRequestContentHistory.create(
                event.pullRequestId(),
                event.headCommitSha(),
                event.changeStats(),
                event.commitCount(),
                event.githubCreatedAt()
        );

        pullRequestContentHistoryRepository.save(pullRequestContentHistory);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveContentHistory(PullRequestSynchronizedEvent event) {
        PullRequestContentHistory pullRequestContentHistory = PullRequestContentHistory.create(
                event.pullRequestId(),
                event.headCommitSha(),
                event.changeStats(),
                event.commitCount(),
                event.githubChangedAt()
        );

        pullRequestContentHistoryRepository.save(pullRequestContentHistory);
    }
}
