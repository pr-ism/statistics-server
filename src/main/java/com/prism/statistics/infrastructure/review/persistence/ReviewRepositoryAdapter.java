package com.prism.statistics.infrastructure.review.persistence;

import com.prism.statistics.domain.review.Review;
import com.prism.statistics.domain.review.repository.ReviewRepository;
import com.prism.statistics.infrastructure.common.MysqlDuplicateKeyDetector;
import com.prism.statistics.infrastructure.review.persistence.exception.ReviewNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewCreator reviewCreator;
    private final JpaReviewRepository jpaReviewRepository;
    private final MysqlDuplicateKeyDetector duplicateKeyDetector;

    @Override
    public Review saveOrFind(Review review) {
        try {
            reviewCreator.saveNew(review);
            return review;
        } catch (DataIntegrityViolationException ex) {
            if (duplicateKeyDetector.isDuplicateKey(ex)) {
                return findByGithubReviewId(review.getGithubReviewId())
                        .orElseThrow(() -> new ReviewNotFoundException());
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findByGithubReviewId(Long githubReviewId) {
        return jpaReviewRepository.findByGithubReviewId(githubReviewId);
    }
}
