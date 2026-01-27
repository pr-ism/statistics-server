package com.prism.statistics.application.webhook.event.listener;

import com.prism.statistics.application.webhook.event.PrOpenCreatedEvent;
import com.prism.statistics.domain.pullrequest.PrStateChangeHistory;
import com.prism.statistics.domain.pullrequest.repository.PrStateChangeHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PrStateChangeHistoryEventListener {

    private final PrStateChangeHistoryRepository prStateChangeHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(PrOpenCreatedEvent event) {
        PrStateChangeHistory history = PrStateChangeHistory.createInitial(
                event.pullRequestId(),
                event.initialState(),
                event.prCreatedAt()
        );

        prStateChangeHistoryRepository.save(history);
    }
}
