package com.prism.statistics.application.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.Author;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitData;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitNode;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitsConnection;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.PullRequestData;
import com.prism.statistics.context.QueryCountInspector;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaCommitRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestFileHistoryRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestFileRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestOpenedBulkInsertTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int FILE_COUNT = 20;
    private static final int COMMIT_COUNT = 8;

    @Autowired
    private PullRequestOpenedService pullRequestOpenedService;

    @Autowired
    private JpaPullRequestRepository jpaPullRequestRepository;

    @Autowired
    private JpaPullRequestFileRepository jpaPullRequestFileRepository;

    @Autowired
    private JpaCommitRepository jpaCommitRepository;

    @Autowired
    private JpaPullRequestFileHistoryRepository jpaPullRequestFileHistoryRepository;

    @Autowired
    private QueryCountInspector queryCountInspector;

    @BeforeEach
    void setUp() {
        queryCountInspector.reset();
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 파일_20개_커밋_8개의_PR_opened_시_INSERT_쿼리가_10회_이하로_실행된다() {
        // given
        PullRequestOpenedRequest request = createLargePullRequestOpenedRequest();

        // when
        pullRequestOpenedService.createPullRequest(TEST_API_KEY, request);

        // then
        int actualInsertCount = queryCountInspector.getInsertQueryCount();
        assertAll(
                () -> assertThat(jpaPullRequestRepository.count()).isEqualTo(1),
                () -> assertThat(jpaCommitRepository.count()).isEqualTo(COMMIT_COUNT),
                () -> assertThat(jpaPullRequestFileRepository.count()).isEqualTo(FILE_COUNT),
                () -> assertThat(jpaPullRequestFileHistoryRepository.count()).isEqualTo(FILE_COUNT),
                () -> assertThat(actualInsertCount).isLessThanOrEqualTo(10)
        );
    }

    private PullRequestOpenedRequest createLargePullRequestOpenedRequest() {
        List<CommitNode> commitNodes = IntStream.range(0, COMMIT_COUNT)
                .mapToObj(i -> new CommitNode(new CommitData(
                        "sha" + String.format("%03d", i),
                        Instant.parse("2024-01-15T09:00:00Z").plusSeconds(i * 60L)
                )))
                .toList();

        PullRequestData pullRequestData = new PullRequestData(
                300L,
                77,
                "대규모 PR Bulk Insert 검증",
                "https://github.com/owner/repo/pull/77",
                "sha000",
                500,
                200,
                FILE_COUNT,
                Instant.parse("2024-01-15T10:00:00Z"),
                new Author("test-author", 1L),
                new CommitsConnection(COMMIT_COUNT, commitNodes)
        );

        List<FileData> files = IntStream.range(0, FILE_COUNT)
                .mapToObj(i -> new FileData(
                        "src/main/java/Feature" + i + ".java",
                        "modified",
                        25,
                        10
                ))
                .toList();

        return new PullRequestOpenedRequest(false, pullRequestData, files);
    }
}
