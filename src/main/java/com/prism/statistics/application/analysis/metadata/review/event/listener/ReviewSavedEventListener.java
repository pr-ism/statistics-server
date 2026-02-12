package com.prism.statistics.application.analysis.metadata.review.event.listener;

import com.prism.statistics.application.analysis.metadata.review.event.ReviewSavedEvent;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ReviewSavedEventListener {

    private final ReviewCommentRepository reviewCommentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void backfillReviewId(ReviewSavedEvent event) {
        reviewCommentRepository.backfillReviewId(event.githubReviewId(), event.reviewId());
    }
}
