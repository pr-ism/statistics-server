package com.prism.statistics.application.analysis.insight.listener;

import com.prism.statistics.application.analysis.insight.PullRequestMetricsPublisher;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSavedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestMetricsEventListener {

    private final PullRequestMetricsPublisher metricsPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PullRequestSavedEvent event) {
        metricsPublisher.publish(event.pullRequestId());
    }
}
