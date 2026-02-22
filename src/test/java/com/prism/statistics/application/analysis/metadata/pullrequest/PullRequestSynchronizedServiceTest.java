package com.prism.statistics.application.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest.CommitNode;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest.CommitsData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSynchronizedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.exception.HeadCommitNotFoundException;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaCommitRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestContentHistoryRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestFileHistoryRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestFileRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;

@IntegrationTest
@RecordApplicationEvents
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestSynchronizedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_PULL_REQUEST_NUMBER = 123;

    @Autowired
    private PullRequestSynchronizedService pullRequestSynchronizedService;

    @Autowired
    private JpaPullRequestRepository jpaPullRequestRepository;

    @Autowired
    private JpaPullRequestFileRepository jpaPullRequestFileRepository;

    @Autowired
    private JpaCommitRepository jpaCommitRepository;

    @Autowired
    private JpaPullRequestContentHistoryRepository jpaPullRequestContentHistoryRepository;

    @Autowired
    private JpaPullRequestFileHistoryRepository jpaPullRequestFileHistoryRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void synchronize_이벤트를_처리하면_PullRequestSynchronizedEvent가_발행된다() {
        // given
        PullRequestSynchronizedRequest request = createNewerRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, request);

        // then
        long eventCount = applicationEvents.stream(PullRequestSynchronizedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void 최신_데이터면_PR_엔티티가_업데이트된다() {
        // given
        PullRequestSynchronizedRequest request = createNewerRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(pullRequest.getHeadCommitSha()).isEqualTo("sha3"),
                () -> assertThat(pullRequest.getCommitCount()).isEqualTo(3),
                () -> assertThat(pullRequest.getChangeStats().getAdditionCount()).isEqualTo(200),
                () -> assertThat(pullRequest.getChangeStats().getDeletionCount()).isEqualTo(80),
                () -> assertThat(pullRequest.getChangeStats().getChangedFileCount()).isEqualTo(15)
        );
    }

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void 오래된_데이터면_PR_엔티티가_업데이트되지_않는다() {
        // given
        PullRequestSynchronizedRequest newerRequest = createNewerRequest();
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, newerRequest);

        PullRequestSynchronizedRequest olderRequest = createOlderRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, olderRequest);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getHeadCommitSha()).isEqualTo("sha3");
    }

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void 새_커밋만_저장되고_기존_커밋은_중복_저장되지_않는다() {
        // given
        PullRequestSynchronizedRequest request = createNewerRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, request);

        // then
        assertThat(jpaCommitRepository.count()).isEqualTo(3);
    }

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void 최신_데이터면_PullRequestFile이_교체된다() {
        // given
        PullRequestSynchronizedRequest request = createNewerRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestFileRepository.count()).isEqualTo(3),
                () -> assertThat(jpaPullRequestFileRepository.findAll().stream()
                        .map(file -> file.getFileName())
                        .toList()
                ).containsExactlyInAnyOrder("src/main/java/NewFile.java", "src/main/java/NewFile2.java", "src/main/java/NewFile3.java")
        );
    }

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void 오래된_데이터면_PullRequestFile이_유지된다() {
        // given
        PullRequestSynchronizedRequest newerRequest = createNewerRequest();
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, newerRequest);

        PullRequestSynchronizedRequest olderRequest = createOlderRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, olderRequest);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestFileRepository.count()).isEqualTo(3),
                () -> assertThat(jpaPullRequestFileRepository.findAll().stream()
                        .map(file -> file.getFileName())
                        .toList()
                ).containsExactlyInAnyOrder("src/main/java/NewFile.java", "src/main/java/NewFile2.java", "src/main/java/NewFile3.java")
        );
    }

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void ContentHistory는_항상_저장된다() {
        // given
        PullRequestSynchronizedRequest newerRequest = createNewerRequest();
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, newerRequest);

        PullRequestSynchronizedRequest olderRequest = createOlderRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, olderRequest);

        // then
        assertThat(jpaPullRequestContentHistoryRepository.count()).isEqualTo(2);
    }

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void FileHistory는_항상_저장된다() {
        // given
        PullRequestSynchronizedRequest newerRequest = createNewerRequest();
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, newerRequest);

        PullRequestSynchronizedRequest olderRequest = createOlderRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, olderRequest);

        // then
        assertThat(jpaPullRequestFileHistoryRepository.count()).isEqualTo(5);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PullRequest가_존재하지_않으면_아무_동작도_하지_않는다() {
        // given
        PullRequestSynchronizedRequest request = createNewerRequest();

        // when
        pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(applicationEvents.stream(PullRequestSynchronizedEvent.class).count()).isEqualTo(0),
                () -> assertThat(jpaCommitRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestContentHistoryRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestFileHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Sql("/sql/webhook/insert_project_pr_commits_and_files.sql")
    @Test
    void headCommitSha에_해당하는_커밋이_없으면_예외가_발생한다() {
        // given
        List<CommitNode> commitNodes = List.of(
                new CommitNode("sha1", Instant.parse("2024-01-15T09:00:00Z")),
                new CommitNode("sha2", Instant.parse("2024-01-15T09:30:00Z"))
        );

        List<FileData> files = List.of(
                new FileData("src/main/java/File.java", "added", 10, 0)
        );

        PullRequestSynchronizedRequest request = new PullRequestSynchronizedRequest(
                TEST_PULL_REQUEST_NUMBER,
                "non-existent-sha",
                10,
                5,
                1,
                new CommitsData(2, commitNodes),
                files
        );

        // when & then
        assertThatThrownBy(() -> pullRequestSynchronizedService.synchronizePullRequest(TEST_API_KEY, request))
                .isInstanceOf(HeadCommitNotFoundException.class);
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        PullRequestSynchronizedRequest request = createNewerRequest();
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> pullRequestSynchronizedService.synchronizePullRequest(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    /**
     * 최신 데이터 요청 (isNewer = true)
     * 현재 PR headCommitSha = "sha1", incoming commits에 sha1이 포함됨 → 최신
     * 기존 커밋: sha1, sha2 / 새 커밋: sha3 (1개만 추가)
     */
    private PullRequestSynchronizedRequest createNewerRequest() {
        List<CommitNode> commitNodes = List.of(
                new CommitNode("sha1", Instant.parse("2024-01-15T09:00:00Z")),
                new CommitNode("sha2", Instant.parse("2024-01-15T09:30:00Z")),
                new CommitNode("sha3", Instant.parse("2024-01-15T10:00:00Z"))
        );

        List<FileData> files = List.of(
                new FileData("src/main/java/NewFile.java", "added", 100, 0),
                new FileData("src/main/java/NewFile2.java", "modified", 60, 40),
                new FileData("src/main/java/NewFile3.java", "added", 40, 40)
        );

        return new PullRequestSynchronizedRequest(
                TEST_PULL_REQUEST_NUMBER,
                "sha3",
                200,
                80,
                15,
                new CommitsData(3, commitNodes),
                files
        );
    }

    /**
     * 오래된 데이터 요청 (isNewer = false)
     * newerRequest 실행 후 headCommitSha = "sha3"이 된 상태에서
     * incoming commits에 sha3이 없음 → 오래된 데이터
     */
    private PullRequestSynchronizedRequest createOlderRequest() {
        List<CommitNode> commitNodes = List.of(
                new CommitNode("sha1", Instant.parse("2024-01-15T09:00:00Z")),
                new CommitNode("sha2", Instant.parse("2024-01-15T09:30:00Z"))
        );

        List<FileData> files = List.of(
                new FileData("src/main/java/OlderFile.java", "added", 30, 0),
                new FileData("src/main/java/OlderFile2.java", "modified", 20, 10)
        );

        return new PullRequestSynchronizedRequest(
                TEST_PULL_REQUEST_NUMBER,
                "sha2",
                50,
                10,
                5,
                new CommitsData(2, commitNodes),
                files
        );
    }
}
