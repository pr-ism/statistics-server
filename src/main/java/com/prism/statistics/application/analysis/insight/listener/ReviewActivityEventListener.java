package com.prism.statistics.application.analysis.insight.listener;

import com.prism.statistics.application.analysis.insight.ReviewActivityPublisher;
import com.prism.statistics.application.analysis.metadata.review.event.ReviewSavedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewActivityEventListener {

    private final ReviewActivityPublisher reviewActivityPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReviewSavedEvent event) {
        reviewActivityPublisher.publish(event.githubReviewId());
    }
}
