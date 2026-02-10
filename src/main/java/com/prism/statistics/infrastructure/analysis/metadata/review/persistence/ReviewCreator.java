package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import com.prism.statistics.application.analysis.metadata.review.event.ReviewSavedEvent;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewCreator {

    private final JpaReviewRepository jpaReviewRepository;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Review saveNew(Review review) {
        Review saved = jpaReviewRepository.save(review);
        entityManager.flush();
        eventPublisher.publishEvent(new ReviewSavedEvent(saved));
        return saved;
    }
}
