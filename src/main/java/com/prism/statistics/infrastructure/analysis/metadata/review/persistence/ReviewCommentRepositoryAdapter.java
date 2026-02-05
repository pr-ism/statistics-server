package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import static com.prism.statistics.domain.analysis.metadata.review.QReviewComment.reviewComment;

import com.prism.statistics.domain.analysis.metadata.review.ReviewComment;
import com.prism.statistics.domain.analysis.metadata.review.repository.ReviewCommentRepository;
import com.prism.statistics.infrastructure.common.MysqlDuplicateKeyDetector;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.exception.ReviewCommentNotFoundException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewCommentRepositoryAdapter implements ReviewCommentRepository {

    private final ReviewCommentCreator reviewCommentCreator;
    private final JpaReviewCommentRepository jpaReviewCommentRepository;
    private final MysqlDuplicateKeyDetector duplicateKeyDetector;
    private final JPAQueryFactory queryFactory;

    @Override
    public ReviewComment saveOrFind(ReviewComment reviewComment) {
        try {
            reviewCommentCreator.saveNew(reviewComment);
            return reviewComment;
        } catch (DataIntegrityViolationException ex) {
            if (duplicateKeyDetector.isDuplicateKey(ex)) {
                return findByGithubCommentId(reviewComment.getGithubCommentId())
                        .orElseThrow(() -> new ReviewCommentNotFoundException());
            }
            throw ex;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewComment> findByGithubCommentId(Long githubCommentId) {
        return jpaReviewCommentRepository.findByGithubCommentId(githubCommentId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByGithubCommentId(Long githubCommentId) {
        return queryFactory
                .selectOne()
                .from(reviewComment)
                .where(reviewComment.githubCommentId.eq(githubCommentId))
                .fetchFirst() != null;
    }

    @Override
    @Transactional
    public long updateBodyIfLatest(Long githubCommentId, String body, LocalDateTime updatedAt) {
        return queryFactory
                .update(reviewComment)
                .set(reviewComment.body, body)
                .set(reviewComment.githubUpdatedAt, updatedAt)
                .where(
                        reviewComment.githubCommentId.eq(githubCommentId),
                        reviewComment.githubUpdatedAt.lt(updatedAt)
                )
                .execute();
    }

    @Override
    @Transactional
    public long softDeleteIfLatest(Long githubCommentId, LocalDateTime updatedAt) {
        return queryFactory
                .update(reviewComment)
                .set(reviewComment.deleted, true)
                .set(reviewComment.githubUpdatedAt, updatedAt)
                .where(
                        reviewComment.githubCommentId.eq(githubCommentId),
                        reviewComment.githubUpdatedAt.lt(updatedAt),
                        reviewComment.deleted.isFalse()
                )
                .execute();
    }
}
