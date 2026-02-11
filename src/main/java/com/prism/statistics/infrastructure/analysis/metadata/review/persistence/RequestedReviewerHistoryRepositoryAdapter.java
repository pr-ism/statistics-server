package com.prism.statistics.infrastructure.analysis.metadata.review.persistence;

import static com.prism.statistics.domain.analysis.metadata.review.history.QRequestedReviewerHistory.requestedReviewerHistory;

import com.prism.statistics.domain.analysis.metadata.review.history.RequestedReviewerHistory;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerHistoryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RequestedReviewerHistoryRepositoryAdapter implements RequestedReviewerHistoryRepository {

    private final JpaRequestedReviewerHistoryRepository jpaRequestedReviewerHistoryRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public RequestedReviewerHistory save(RequestedReviewerHistory requestedReviewerHistory) {
        return jpaRequestedReviewerHistoryRepository.save(requestedReviewerHistory);
    }

    @Override
    @Transactional
    public long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId) {
        return queryFactory
                .update(requestedReviewerHistory)
                .set(requestedReviewerHistory.pullRequestId, pullRequestId)
                .where(
                        requestedReviewerHistory.githubPullRequestId.eq(githubPullRequestId),
                        requestedReviewerHistory.pullRequestId.isNull()
                )
                .execute();
    }
}
