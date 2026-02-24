package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QCommit.commit;

import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.CommitRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommitRepositoryAdapter implements CommitRepository {

    private final JpaCommitRepository jpaCommitRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public Commit save(Commit commitEntity) {
        return jpaCommitRepository.save(commitEntity);
    }

    @Override
    @Transactional
    public List<Commit> saveAll(List<Commit> commits) {
        return jpaCommitRepository.saveAll(commits);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> findAllCommitShasByPullRequestId(Long pullRequestId) {
        return queryFactory
                .select(commit.commitSha)
                .from(commit)
                .where(commit.pullRequestId.eq(pullRequestId))
                .fetch()
                .stream()
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional
    public long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId) {
        return queryFactory
                .update(commit)
                .set(commit.pullRequestId, pullRequestId)
                .where(
                        commit.githubPullRequestId.eq(githubPullRequestId),
                        commit.pullRequestId.isNull()
                )
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public Set<String> findAllCommitShasByGithubPullRequestId(Long githubPullRequestId) {
        return queryFactory
                .select(commit.commitSha)
                .from(commit)
                .where(commit.githubPullRequestId.eq(githubPullRequestId))
                .fetch()
                .stream()
                .collect(Collectors.toUnmodifiableSet());
    }
}
