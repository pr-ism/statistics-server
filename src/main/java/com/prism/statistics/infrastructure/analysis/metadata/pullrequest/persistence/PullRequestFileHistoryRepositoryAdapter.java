package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.history.QPullRequestFileHistory.pullRequestFileHistory;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestFileHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileHistoryRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PullRequestFileHistoryRepositoryAdapter implements PullRequestFileHistoryRepository {

    private final JpaPullRequestFileHistoryRepository jpaPullRequestFileHistoryRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public PullRequestFileHistory save(PullRequestFileHistory pullRequestFileHistory) {
        return jpaPullRequestFileHistoryRepository.save(pullRequestFileHistory);
    }

    @Override
    @Transactional
    public List<PullRequestFileHistory> saveAll(List<PullRequestFileHistory> pullRequestFileHistories) {
        return jpaPullRequestFileHistoryRepository.saveAll(pullRequestFileHistories);
    }

    @Override
    @Transactional
    public long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId) {
        return queryFactory
                .update(pullRequestFileHistory)
                .set(pullRequestFileHistory.pullRequestId, pullRequestId)
                .where(
                        pullRequestFileHistory.githubPullRequestId.eq(githubPullRequestId),
                        pullRequestFileHistory.pullRequestId.isNull()
                )
                .execute();
    }
}
