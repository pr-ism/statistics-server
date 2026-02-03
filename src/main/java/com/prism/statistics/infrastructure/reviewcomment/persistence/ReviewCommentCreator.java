package com.prism.statistics.infrastructure.reviewcomment.persistence;

import com.prism.statistics.domain.reviewcomment.ReviewComment;
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
    public void saveNew(ReviewComment reviewComment) {
        jpaReviewCommentRepository.save(reviewComment);
        entityManager.flush();
    }
}
