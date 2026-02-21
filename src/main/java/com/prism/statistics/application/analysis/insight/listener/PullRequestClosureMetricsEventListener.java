package com.prism.statistics.application.analysis.insight.listener;

import com.prism.statistics.application.analysis.insight.PullRequestClosureMetricsEvent;
import com.prism.statistics.application.analysis.insight.PullRequestClosureMetricsPublisher;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestStateChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PullRequestClosureMetricsEventListener {

    private final PullRequestClosureMetricsPublisher closureMetricsPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PullRequestStateChangedEvent event) {
        if (!event.newState().isClosureState()) {
            return;
        }

        closureMetricsPublisher.publish(new PullRequestClosureMetricsEvent(
                event.pullRequestId(),
                event.newState(),
                event.githubChangedAt()
        ));
    }
}
