package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.history.QPullRequestFileHistory.pullRequestFileHistory;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestFileHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileHistoryRepository;
import com.prism.statistics.global.config.properties.BatchInsertProperties;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
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
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final Clock clock;
    private final BatchInsertProperties batchInsertProperties;

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
                ) VALUES (
                    :githubPullRequestId,
                    :pullRequestId,
                    :headCommitSha,
                    :fileName,
                    :previousFileName,
                    :changeType,
                    :additions,
                    :deletions,
                    :githubChangedAt,
                    :createdAt
                )
                """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now(clock));
        List<SqlParameterSource> parameterSources = pullRequestFileHistories.stream()
                .map(history -> toParameterSource(history, now))
                .toList();

        batchUpdateInChunks(sql, parameterSources);
    }

    private SqlParameterSource toParameterSource(PullRequestFileHistory history, Timestamp now) {
        return new MapSqlParameterSource()
                .addValue("githubPullRequestId", history.getGithubPullRequestId())
                .addValue("pullRequestId", history.getPullRequestId(), Types.BIGINT)
                .addValue("headCommitSha", history.getHeadCommitSha())
                .addValue("fileName", history.getFileName())
                .addValue("previousFileName", history.getPreviousFileName().getValue(), Types.VARCHAR)
                .addValue("changeType", history.getChangeType().name())
                .addValue("additions", history.getFileChanges().getAdditions())
                .addValue("deletions", history.getFileChanges().getDeletions())
                .addValue("githubChangedAt", Timestamp.valueOf(history.getGithubChangedAt()))
                .addValue("createdAt", now);
    }

    private void batchUpdateInChunks(String sql, List<SqlParameterSource> parameterSources) {
        int chunkSize = batchInsertProperties.chunkSize();
        for (int start = 0; start < parameterSources.size(); start += chunkSize) {
            int end = Math.min(start + chunkSize, parameterSources.size());
            SqlParameterSource[] chunk = parameterSources.subList(start, end).toArray(SqlParameterSource[]::new);
            namedParameterJdbcTemplate.batchUpdate(sql, chunk);
        }
    }
}
