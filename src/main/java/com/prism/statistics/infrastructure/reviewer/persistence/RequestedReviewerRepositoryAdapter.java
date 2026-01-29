package com.prism.statistics.infrastructure.reviewer.persistence;

import static com.prism.statistics.domain.reviewer.QRequestedReviewer.requestedReviewer;

import com.prism.statistics.domain.reviewer.RequestedReviewer;
import com.prism.statistics.domain.reviewer.repository.RequestedReviewerRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class RequestedReviewerRepositoryAdapter implements RequestedReviewerRepository {

    private final JpaRequestedReviewerRepository jpaRequestedReviewerRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public RequestedReviewer save(RequestedReviewer requestedReviewer) {
        return jpaRequestedReviewerRepository.save(requestedReviewer);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(Long pullRequestId, Long githubUid) {
        return queryFactory
                .selectOne()
                .from(requestedReviewer)
                .where(
                        requestedReviewer.pullRequestId.eq(pullRequestId),
                        requestedReviewer.githubUid.eq(githubUid)
                )
                .fetchFirst() != null;
    }

    @Override
    @Transactional
    public long delete(Long pullRequestId, Long githubUid) {
        return queryFactory
                .delete(requestedReviewer)
                .where(
                        requestedReviewer.pullRequestId.eq(pullRequestId),
                        requestedReviewer.githubUid.eq(githubUid)
                )
                .execute();
    }
}
