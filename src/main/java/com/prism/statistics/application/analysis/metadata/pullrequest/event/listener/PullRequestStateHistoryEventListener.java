package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
    import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestStateChangedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestStateHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestStateHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestStateHistoryEventListener {

    private final PullRequestStateHistoryRepository pullRequestStateHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveInitialHistory(PullRequestOpenCreatedEvent event) {
        PullRequestStateHistory pullRequestStateHistory = PullRequestStateHistory.createInitial(
                event.pullRequestId(),
                event.headCommitSha(),
                event.initialState(),
                event.githubCreatedAt()
        );

        pullRequestStateHistoryRepository.save(pullRequestStateHistory);
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void saveStateChangeHistory(PullRequestStateChangedEvent event) {
        PullRequestStateHistory pullRequestStateHistory = PullRequestStateHistory.create(
                event.pullRequestId(),
                event.headCommitSha(),
                event.previousState(),
                event.newState(),
                event.githubChangedAt()
        );

        pullRequestStateHistoryRepository.save(pullRequestStateHistory);
    }
}
