package com.prism.statistics.infrastructure.analysis.insight.persistence;

import static com.prism.statistics.domain.analysis.insight.review.QReviewSession.reviewSession;

import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.insight.review.repository.ReviewSessionRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ReviewSessionRepositoryAdapter implements ReviewSessionRepository {

    private final JpaReviewSessionRepository jpaReviewSessionRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public ReviewSession save(ReviewSession session) {
        return jpaReviewSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewSession> findByPullRequestId(Long pullRequestId) {
        return jpaReviewSessionRepository.findByPullRequestId(pullRequestId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewSession> findByReviewer(Long pullRequestId, Long reviewerGithubId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(reviewSession)
                        .where(
                                reviewSession.pullRequestId.eq(pullRequestId),
                                reviewSession.reviewer.userId.eq(reviewerGithubId)
                        )
                        .fetchOne()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByReviewer(Long pullRequestId, Long reviewerGithubId) {
        return queryFactory
                .selectOne()
                .from(reviewSession)
                .where(
                        reviewSession.pullRequestId.eq(pullRequestId),
                        reviewSession.reviewer.userId.eq(reviewerGithubId)
                )
                .fetchFirst() != null;
    }
}
