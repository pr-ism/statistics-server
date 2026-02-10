package com.prism.statistics.application.analysis.metadata.pullrequest.event.listener;

import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
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
    public void handle(PullRequestOpenCreatedEvent event) {
        PullRequestStateHistory pullRequestStateHistory = PullRequestStateHistory.createInitial(
                event.pullRequestId(),
                event.headCommitSha(),
                event.initialState(),
                event.pullRequestCreatedAt()
        );

        pullRequestStateHistoryRepository.save(pullRequestStateHistory);
    }
}
