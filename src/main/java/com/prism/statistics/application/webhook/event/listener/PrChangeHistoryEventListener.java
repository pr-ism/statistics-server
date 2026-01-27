package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.PrChangeHistory;
import com.prism.statistics.domain.pullrequest.repository.PrChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PrChangeHistoryEventListener {

    private final PrChangeHistoryRepository prChangeHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PrOpenCreatedEvent event) {
        PrChangeHistory history = PrChangeHistory.create(
                event.pullRequestId(),
                event.changeStats(),
                event.commitCount(),
                event.prCreatedAt()
        );

        prChangeHistoryRepository.save(history);
    }
}
