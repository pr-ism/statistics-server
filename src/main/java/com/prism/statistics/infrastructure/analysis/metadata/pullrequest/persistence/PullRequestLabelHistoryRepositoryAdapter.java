package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.history.QPullRequestLabelHistory.pullRequestLabelHistory;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestLabelHistoryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class PullRequestLabelHistoryRepositoryAdapter implements PullRequestLabelHistoryRepository {

    private final JpaPullRequestLabelHistoryRepository jpaPullRequestLabelHistoryRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public PullRequestLabelHistory save(PullRequestLabelHistory pullRequestLabelHistory) {
        return jpaPullRequestLabelHistoryRepository.save(pullRequestLabelHistory);
    }

    @Override
    @Transactional
    public long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId) {
        return queryFactory
                .update(pullRequestLabelHistory)
                .set(pullRequestLabelHistory.pullRequestId, pullRequestId)
                .where(
                        pullRequestLabelHistory.githubPullRequestId.eq(githubPullRequestId),
                        pullRequestLabelHistory.pullRequestId.isNull()
                )
                .execute();
    }
}
