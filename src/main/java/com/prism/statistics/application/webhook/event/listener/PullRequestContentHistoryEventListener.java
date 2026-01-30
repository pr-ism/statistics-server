package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.PullRequestContentHistory;
import com.prism.statistics.domain.pullrequest.repository.PullRequestContentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestContentHistoryEventListener {

    private final PullRequestContentHistoryRepository pullRequestContentHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PullRequestOpenCreatedEvent event) {
        PullRequestContentHistory pullRequestContentHistory = PullRequestContentHistory.create(
                event.pullRequestId(),
                event.changeStats(),
                event.commitCount(),
                event.prCreatedAt()
        );

        pullRequestContentHistoryRepository.save(pullRequestContentHistory);
    }
}
