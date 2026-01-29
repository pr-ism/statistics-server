package com.prism.statistics.application.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.Author;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.CommitData;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.CommitNode;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.CommitsConnection;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.FileData;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.PullRequestData;
import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.enums.PrState;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaCommitRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPrChangeHistoryRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPrFileHistoryRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPrFileRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPrStateChangeHistoryRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrOpenedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private PrOpenedService prOpenedService;

    @Autowired
    private JpaPullRequestRepository jpaPullRequestRepository;

    @Autowired
    private JpaPrFileRepository jpaPrFileRepository;

    @Autowired
    private JpaCommitRepository jpaCommitRepository;

    @Autowired
    private JpaPrStateChangeHistoryRepository jpaPrStateChangeHistoryRepository;

    @Autowired
    private JpaPrChangeHistoryRepository jpaPrChangeHistoryRepository;

    @Autowired
    private JpaPrFileHistoryRepository jpaPrFileHistoryRepository;

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PR_opened_이벤트를_처리하면_모든_엔티티가_저장된다() {
        // given
        PrOpenedRequest request = createPrOpenedRequest(false);

        // when
        prOpenedService.handle(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPrFileRepository.count()).isEqualTo(2),
                () -> assertThat(jpaCommitRepository.count()).isEqualTo(2),
                () -> assertThat(jpaPrStateChangeHistoryRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPrChangeHistoryRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPrFileHistoryRepository.count()).isEqualTo(2)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PR_opened_이벤트를_처리하면_OPEN_상태로_저장된다() {
        // given
        PrOpenedRequest request = createPrOpenedRequest(false);

        // when
        prOpenedService.handle(TEST_API_KEY, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getState()).isEqualTo(PrState.OPEN);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Draft_PR이면_엔티티가_저장되지_않는다() {
        // given
        PrOpenedRequest request = createPrOpenedRequest(true);

        // when
        prOpenedService.handle(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPrFileRepository.count()).isEqualTo(0),
                () -> assertThat(jpaCommitRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPrStateChangeHistoryRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPrChangeHistoryRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPrFileHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        PrOpenedRequest request = createPrOpenedRequest(false);
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> prOpenedService.handle(invalidApiKey, request))
                .isInstanceOf(ProjectNotFoundException.class)
                .hasMessage("유효하지 않은 API Key입니다.");
    }

    private PrOpenedRequest createPrOpenedRequest(boolean isDraft) {
        List<CommitNode> commitNodes = List.of(
                new CommitNode(new CommitData("abc123", Instant.parse("2024-01-15T09:00:00Z"))),
                new CommitNode(new CommitData("def456", Instant.parse("2024-01-15T09:30:00Z")))
        );

        PullRequestData pullRequestData = new PullRequestData(
                42,
                "테스트 PR 제목",
                "https://github.com/owner/repo/pull/42",
                100,
                50,
                10,
                Instant.parse("2024-01-15T10:00:00Z"),
                new Author("test-author"),
                new CommitsConnection(2, commitNodes)
        );

        List<FileData> files = List.of(
                new FileData("src/main/java/Example.java", "modified", 80, 30),
                new FileData("src/main/java/NewFile.java", "added", 20, 0)
        );

        return new PrOpenedRequest(
                "pull_request",
                "opened",
                "owner/repo",
                isDraft,
                pullRequestData,
                files
        );
    }
}
