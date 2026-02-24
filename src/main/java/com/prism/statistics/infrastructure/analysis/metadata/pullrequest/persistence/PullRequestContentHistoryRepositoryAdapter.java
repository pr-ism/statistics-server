package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.history.QPullRequestContentHistory.pullRequestContentHistory;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestContentHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestContentHistoryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestContentHistoryRepositoryAdapter implements PullRequestContentHistoryRepository {

    private final JpaPullRequestContentHistoryRepository jpaPullRequestContentHistoryRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public PullRequestContentHistory save(PullRequestContentHistory pullRequestContentHistory) {
        return jpaPullRequestContentHistoryRepository.save(pullRequestContentHistory);
    }

    @Override
    @Transactional
    public long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId) {
        return queryFactory
                .update(pullRequestContentHistory)
                .set(pullRequestContentHistory.pullRequestId, pullRequestId)
                .where(
                        pullRequestContentHistory.githubPullRequestId.eq(githubPullRequestId),
                        pullRequestContentHistory.pullRequestId.isNull()
                )
                .execute();
    }
}
