package com.prism.statistics.infrastructure.review.persistence;

import com.prism.statistics.domain.review.Review;
import com.prism.statistics.domain.review.repository.ReviewRepository;
import com.prism.statistics.infrastructure.common.MysqlDuplicateKeyDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewRepositoryAdapter implements ReviewRepository {

    private final ReviewCreator reviewCreator;
    private final MysqlDuplicateKeyDetector duplicateKeyDetector;

    @Override
    public Review save(Review review) {
        try {
            reviewCreator.saveNew(review);
            return review;
        } catch (DataIntegrityViolationException ex) {
            if (duplicateKeyDetector.isDuplicateKey(ex)) {
                return null;
            }
            throw ex;
        }
    }
}
