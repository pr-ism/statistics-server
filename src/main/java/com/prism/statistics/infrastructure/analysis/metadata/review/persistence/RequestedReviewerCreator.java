package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import com.prism.statistics.domain.analysis.metadata.review.RequestedReviewer;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RequestedReviewerCreator {

    private final JpaRequestedReviewerRepository jpaRequestedReviewerRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RequestedReviewer saveNew(RequestedReviewer requestedReviewer) {
        RequestedReviewer saved = jpaRequestedReviewerRepository.save(requestedReviewer);
        entityManager.flush();
        return saved;
    }
}
