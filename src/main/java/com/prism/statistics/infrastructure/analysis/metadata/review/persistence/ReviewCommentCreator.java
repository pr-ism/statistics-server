package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewCommentCreator {

    private final JpaReviewCommentRepository jpaReviewCommentRepository;
    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReviewComment saveNew(ReviewComment reviewComment) {
        ReviewComment saved = jpaReviewCommentRepository.save(reviewComment);
        entityManager.flush();
        return saved;
    }
}
