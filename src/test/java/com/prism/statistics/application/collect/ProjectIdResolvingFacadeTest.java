package com.prism.statistics.application.collect;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestConvertedToDraftRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.Author;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitData;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitNode;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitsConnection;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.PullRequestData;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReadyForReviewRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReopenedRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectIdResolvingFacadeTest {

    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ProjectIdResolvingFacade projectIdResolvingFacade;

    @Autowired
    private JpaPullRequestRepository jpaPullRequestRepository;

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void apiKey를_projectId로_변환하여_PullRequest를_생성한다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest();

        // when
        projectIdResolvingFacade.createPullRequest(TEST_API_KEY, request);

        // then
        assertThat(jpaPullRequestRepository.count()).isEqualTo(1);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void apiKey를_projectId로_변환하여_PullRequest를_닫는다() {
        // given
        PullRequestClosedRequest request = new PullRequestClosedRequest(
                null, 123, false, Instant.now(), null
        );

        // when
        projectIdResolvingFacade.closePullRequest(TEST_API_KEY, request);

        // then
        assertThat(jpaPullRequestRepository.findAll().getFirst().getState())
                .isEqualTo(PullRequestState.CLOSED);
    }

    @Sql("/sql/webhook/insert_project_and_draft_pull_request.sql")
    @Test
    void apiKey를_projectId로_변환하여_PullRequest를_리뷰_준비_상태로_변경한다() {
        // given
        PullRequestReadyForReviewRequest request = new PullRequestReadyForReviewRequest(
                null, 123, Instant.now()
        );

        // when
        projectIdResolvingFacade.readyForReview(TEST_API_KEY, request);

        // then
        assertThat(jpaPullRequestRepository.findAll().getFirst().getState())
                .isEqualTo(PullRequestState.OPEN);
    }

    @Sql("/sql/webhook/insert_project_and_closed_pull_request.sql")
    @Test
    void apiKey를_projectId로_변환하여_PullRequest를_다시_연다() {
        // given
        PullRequestReopenedRequest request = new PullRequestReopenedRequest(
                null, 123, Instant.now()
        );

        // when
        projectIdResolvingFacade.reopenPullRequest(TEST_API_KEY, request);

        // then
        assertThat(jpaPullRequestRepository.findAll().getFirst().getState())
                .isEqualTo(PullRequestState.OPEN);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void apiKey를_projectId로_변환하여_PullRequest를_Draft로_변환한다() {
        // given
        PullRequestConvertedToDraftRequest request = new PullRequestConvertedToDraftRequest(
                null, 123, Instant.now()
        );

        // when
        projectIdResolvingFacade.convertToDraft(TEST_API_KEY, request);

        // then
        assertThat(jpaPullRequestRepository.findAll().getFirst().getState())
                .isEqualTo(PullRequestState.DRAFT);
    }

    @Test
    void 잘못된_apiKey로_호출하면_예외가_발생한다() {
        // given
        PullRequestClosedRequest request = new PullRequestClosedRequest(
                null, 123, false, Instant.now(), null
        );

        // when & then
        assertThatThrownBy(() -> projectIdResolvingFacade.closePullRequest("invalid-api-key", request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    private PullRequestOpenedRequest createPullRequestOpenedRequest() {
        List<CommitNode> commitNodes = List.of(
                new CommitNode(new CommitData("abc123", Instant.parse("2024-01-15T09:00:00Z"))),
                new CommitNode(new CommitData("def456", Instant.parse("2024-01-15T09:30:00Z")))
        );

        PullRequestData pullRequestData = new PullRequestData(
                100L,
                42,
                "테스트 PR 제목",
                "https://github.com/owner/repo/pull/42",
                "abc123",
                100,
                50,
                10,
                Instant.parse("2024-01-15T10:00:00Z"),
                new Author("test-author", 1L),
                new CommitsConnection(2, commitNodes)
        );

        List<FileData> files = List.of(
                new FileData("src/main/java/Example.java", "modified", 80, 30),
                new FileData("src/main/java/NewFile.java", "added", 20, 0)
        );

        return new PullRequestOpenedRequest(null, false, pullRequestData, files);
    }
}
