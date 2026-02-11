package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import static com.prism.statistics.domain.analysis.metadata.review.QReview.review;

import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewRepository;
import com.prism.statistics.infrastructure.common.MysqlDuplicateKeyDetector;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception.ReviewNotFoundException;
import com.querydsl.jpa.impl.JPAQueryFactory;
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
    private final JPAQueryFactory queryFactory;
    private final MysqlDuplicateKeyDetector duplicateKeyDetector;

    @Override
    public Review saveOrFind(Review review) {
        try {
            return reviewCreator.saveNew(review);
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

    @Override
    @Transactional(readOnly = true)
    public Optional<Long> findIdByGithubReviewId(Long githubReviewId) {
        return Optional.ofNullable(
                queryFactory
                        .select(review.id)
                        .from(review)
                        .where(review.githubReviewId.eq(githubReviewId))
                        .fetchOne()
        );
    }

    @Override
    @Transactional
    public long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId) {
        return queryFactory
                .update(review)
                .set(review.pullRequestId, pullRequestId)
                .where(
                        review.githubPullRequestId.eq(githubPullRequestId),
                        review.pullRequestId.isNull()
                )
                .execute();
    }
}
