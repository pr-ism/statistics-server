package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.PullRequestHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestHistoryEventListener {

    private final PullRequestHistoryRepository pullRequestHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PrOpenCreatedEvent event) {
        PullRequestHistory history = PullRequestHistory.create(
                event.pullRequestId(),
                event.changeStats(),
                event.commitCount(),
                event.prCreatedAt()
        );

        pullRequestHistoryRepository.save(history);
    }
}
