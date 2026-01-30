package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.PullRequestStateHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestStateHistoryRepository;
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
        PullRequestStateHistory pullRequestStateHistoryhistory = PullRequestStateHistory.createInitial(
                event.pullRequestId(),
                event.initialState(),
                event.pullRequestCreatedAt()
        );

        pullRequestStateHistoryRepository.save(pullRequestStateHistoryhistory);
    }
}
