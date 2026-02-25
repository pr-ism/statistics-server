package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.history.QPullRequestFileHistory.pullRequestFileHistory;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestFileHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileHistoryRepository;
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

@Repository
@RequiredArgsConstructor
public class PullRequestFileHistoryRepositoryAdapter implements PullRequestFileHistoryRepository {

    private final JpaPullRequestFileHistoryRepository jpaPullRequestFileHistoryRepository;
    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;

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

    @Override
    @Transactional
    public void saveAllInBatch(List<PullRequestFileHistory> pullRequestFileHistories) {
        if (pullRequestFileHistories.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO pull_request_file_histories (
                    github_pull_request_id,
                    pull_request_id,
                    head_commit_sha,
                    file_name,
                    previous_file_name,
                    change_type,
                    additions,
                    deletions,
                    github_changed_at,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now(clock));

        jdbcTemplate.batchUpdate(sql, pullRequestFileHistories, pullRequestFileHistories.size(), (ps, history) -> {
            ps.setLong(1, history.getGithubPullRequestId());
            ps.setObject(2, history.getPullRequestId(), Types.BIGINT);
            ps.setString(3, history.getHeadCommitSha());
            ps.setString(4, history.getFileName());
            ps.setObject(5, history.getPreviousFileName().getValue(), Types.VARCHAR);
            ps.setString(6, history.getChangeType().name());
            ps.setInt(7, history.getFileChanges().getAdditions());
            ps.setInt(8, history.getFileChanges().getDeletions());
            ps.setTimestamp(9, Timestamp.valueOf(history.getGithubChangedAt()));
            ps.setTimestamp(10, now);
        });
    }
}
