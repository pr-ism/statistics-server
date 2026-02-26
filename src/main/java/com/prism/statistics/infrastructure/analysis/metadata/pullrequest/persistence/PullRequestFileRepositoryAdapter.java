package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static com.prism.statistics.domain.analysis.metadata.pullrequest.QPullRequestFile.pullRequestFile;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileRepository;
import com.prism.statistics.global.config.properties.BatchInsertProperties;
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
public class PullRequestFileRepositoryAdapter implements PullRequestFileRepository {

    private final JpaPullRequestFileRepository jpaPullRequestFileRepository;
    private final JPAQueryFactory queryFactory;
    private final JdbcTemplate jdbcTemplate;
    private final Clock clock;
    private final BatchInsertProperties batchInsertProperties;

    @Override
    @Transactional
    public PullRequestFile save(PullRequestFile pullRequestFileEntity) {
        return jpaPullRequestFileRepository.save(pullRequestFileEntity);
    }

    @Override
    @Transactional
    public List<PullRequestFile> saveAll(List<PullRequestFile> pullRequestFiles) {
        return jpaPullRequestFileRepository.saveAll(pullRequestFiles);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PullRequestFile> findAllByPullRequestId(Long pullRequestId) {
        return queryFactory
                .selectFrom(pullRequestFile)
                .where(pullRequestFile.pullRequestId.eq(pullRequestId))
                .fetch();
    }

    @Override
    @Transactional
    public void deleteAllByPullRequestId(Long pullRequestId) {
        queryFactory
                .delete(pullRequestFile)
                .where(pullRequestFile.pullRequestId.eq(pullRequestId))
                .execute();
    }

    @Override
    @Transactional
    public long backfillPullRequestId(Long githubPullRequestId, Long pullRequestId) {
        return queryFactory
                .update(pullRequestFile)
                .set(pullRequestFile.pullRequestId, pullRequestId)
                .where(
                        pullRequestFile.githubPullRequestId.eq(githubPullRequestId),
                        pullRequestFile.pullRequestId.isNull()
                )
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByGithubPullRequestId(Long githubPullRequestId) {
        return queryFactory
                .selectOne()
                .from(pullRequestFile)
                .where(pullRequestFile.githubPullRequestId.eq(githubPullRequestId))
                .fetchFirst() != null;
    }

    @Override
    @Transactional
    public void saveAllInBatch(List<PullRequestFile> pullRequestFiles) {
        if (pullRequestFiles.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO pull_request_files (
                    github_pull_request_id,
                    pull_request_id,
                    file_name,
                    change_type,
                    additions,
                    deletions,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        Timestamp now = Timestamp.valueOf(LocalDateTime.now(clock));

        jdbcTemplate.batchUpdate(sql, pullRequestFiles, batchInsertProperties.chunkSize(), (ps, file) -> {
            ps.setLong(1, file.getGithubPullRequestId());
            ps.setObject(2, file.getPullRequestId(), Types.BIGINT);
            ps.setString(3, file.getFileName());
            ps.setString(4, file.getChangeType().name());
            ps.setInt(5, file.getFileChanges().getAdditions());
            ps.setInt(6, file.getFileChanges().getDeletions());
            ps.setTimestamp(7, now);
        });
    }
}
