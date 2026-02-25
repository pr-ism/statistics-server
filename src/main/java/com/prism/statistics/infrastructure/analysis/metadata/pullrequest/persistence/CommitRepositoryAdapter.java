package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QCommit.commit;

import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.CommitRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CommitRepositoryAdapter implements CommitRepository {

    private final JpaCommitRepository jpaCommitRepository;
    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

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

    @Override
    @Transactional
    public void saveAllInBatch(List<Commit> commits) {
        if (commits.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO commits (
                    github_pull_request_id,
                    pull_request_id,
                    commit_sha,
                    committed_at,
                    created_at
                ) VALUES (?, ?, ?, ?, ?)
                """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now(clock));

        jdbcTemplate.batchUpdate(sql, commits, commits.size(), (ps, commit) -> {
            ps.setLong(1, commit.getGithubPullRequestId());
            ps.setObject(2, commit.getPullRequestId(), Types.BIGINT);
            ps.setString(3, commit.getCommitSha());
            ps.setTimestamp(4, Timestamp.valueOf(commit.getCommittedAt()));
            ps.setTimestamp(5, now);
        });
    }
}
