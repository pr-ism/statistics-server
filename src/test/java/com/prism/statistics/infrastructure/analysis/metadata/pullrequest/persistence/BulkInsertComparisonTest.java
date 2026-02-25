package com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.context.QueryCountInspector;
import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestFileHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.CommitRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.FileChanges;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class BulkInsertComparisonTest {

    @Autowired
    private CommitRepository commitRepository;

    @Autowired
    private PullRequestFileRepository pullRequestFileRepository;

    @Autowired
    private PullRequestFileHistoryRepository pullRequestFileHistoryRepository;

    @Autowired
    private QueryCountInspector queryCountInspector;

    @BeforeEach
    void setUp() {
        queryCountInspector.reset();
    }

    @Test
    void Commit_saveAll은_엔티티_수만큼_개별_INSERT가_실행된다() {
        // given
        int commitCount = 20;
        List<Commit> commits = createCommits(1L, 100L, commitCount);

        // when
        commitRepository.saveAll(commits);

        // then
        assertThat(queryCountInspector.getInsertQueryCount()).isEqualTo(commitCount);
    }

    @Test
    void Commit_saveAllInBatch는_단일_INSERT로_실행된다() {
        // given
        int commitCount = 20;
        List<Commit> commits = createCommits(1L, 200L, commitCount);

        // when
        commitRepository.saveAllInBatch(commits);

        // then
        assertThat(queryCountInspector.getInsertQueryCount()).isEqualTo(1);
    }

    @Test
    void PullRequestFile_saveAll은_엔티티_수만큼_개별_INSERT가_실행된다() {
        // given
        int fileCount = 50;
        List<PullRequestFile> files = createPullRequestFiles(1L, 100L, fileCount);

        // when
        pullRequestFileRepository.saveAll(files);

        // then
        assertThat(queryCountInspector.getInsertQueryCount()).isEqualTo(fileCount);
    }

    @Test
    void PullRequestFile_saveAllInBatch는_단일_INSERT로_실행된다() {
        // given
        int fileCount = 50;
        List<PullRequestFile> files = createPullRequestFiles(1L, 200L, fileCount);

        // when
        pullRequestFileRepository.saveAllInBatch(files);

        // then
        assertThat(queryCountInspector.getInsertQueryCount()).isEqualTo(1);
    }

    @Test
    void PullRequestFileHistory_saveAll은_엔티티_수만큼_개별_INSERT가_실행된다() {
        // given
        int fileCount = 50;
        List<PullRequestFileHistory> histories = createPullRequestFileHistories(1L, 100L, fileCount);

        // when
        pullRequestFileHistoryRepository.saveAll(histories);

        // then
        assertThat(queryCountInspector.getInsertQueryCount()).isEqualTo(fileCount);
    }

    @Test
    void PullRequestFileHistory_saveAllInBatch는_단일_INSERT로_실행된다() {
        // given
        int fileCount = 50;
        List<PullRequestFileHistory> histories = createPullRequestFileHistories(1L, 200L, fileCount);

        // when
        pullRequestFileHistoryRepository.saveAllInBatch(histories);

        // then
        assertThat(queryCountInspector.getInsertQueryCount()).isEqualTo(1);
    }

    private List<Commit> createCommits(Long pullRequestId, Long githubPullRequestId, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> Commit.create(
                        pullRequestId,
                        githubPullRequestId,
                        "sha" + String.format("%03d", i),
                        LocalDateTime.of(2024, 1, 15, 9, 0).plusMinutes(i)
                ))
                .toList();
    }

    private List<PullRequestFile> createPullRequestFiles(Long pullRequestId, Long githubPullRequestId, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> PullRequestFile.create(
                        pullRequestId,
                        githubPullRequestId,
                        "src/main/java/File" + i + ".java",
                        FileChangeType.MODIFIED,
                        FileChanges.create(10, 5)
                ))
                .toList();
    }

    private List<PullRequestFileHistory> createPullRequestFileHistories(
            Long pullRequestId,
            Long githubPullRequestId,
            int count
    ) {
        return IntStream.range(0, count)
                .mapToObj(i -> PullRequestFileHistory.create(
                        pullRequestId,
                        githubPullRequestId,
                        "headSha123",
                        "src/main/java/File" + i + ".java",
                        FileChangeType.MODIFIED,
                        FileChanges.create(10, 5),
                        LocalDateTime.of(2024, 1, 15, 10, 0)
                ))
                .toList();
    }
}
