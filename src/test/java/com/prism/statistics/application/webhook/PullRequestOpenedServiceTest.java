package com.prism.statistics.application.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.webhook.event.PullRequestDraftCreatedEvent;
import com.prism.statistics.application.webhook.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.Author;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.CommitData;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.CommitNode;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.CommitsConnection;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.PullRequestData;
import com.prism.statistics.domain.pullrequest.PullRequest;
import com.prism.statistics.domain.pullrequest.enums.PullRequestState;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaCommitRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPullRequestContentHistoryRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPullRequestFileHistoryRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPullRequestFileRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPullRequestStateHistoryRepository;
import com.prism.statistics.infrastructure.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
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
class PullRequestOpenedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private PullRequestOpenedService pullRequestOpenedService;

    @Autowired
    private JpaPullRequestRepository jpaPullRequestRepository;

    @Autowired
    private JpaPullRequestFileRepository jpaPullRequestFileRepository;

    @Autowired
    private JpaCommitRepository jpaCommitRepository;

    @Autowired
    private JpaPullRequestStateHistoryRepository jpaPullRequestStateHistoryRepository;

    @Autowired
    private JpaPullRequestContentHistoryRepository jpaPullRequestContentHistoryRepository;

    @Autowired
    private JpaPullRequestFileHistoryRepository jpaPullRequestFileHistoryRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Pull_Request_opened_이벤트를_처리하면_모든_엔티티가_저장된다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(false);

        // when
        pullRequestOpenedService.createPullRequest(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestFileRepository.count()).isEqualTo(2),
                () -> assertThat(jpaCommitRepository.count()).isEqualTo(2),
                () -> assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestContentHistoryRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestFileHistoryRepository.count()).isEqualTo(2)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Pull_Request_opened_이벤트를_처리하면_OPEN_상태로_저장된다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(false);

        // when
        pullRequestOpenedService.createPullRequest(TEST_API_KEY, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getState()).isEqualTo(PullRequestState.OPEN);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Draft_Pull_Request_이면_엔티티가_저장되지_않는다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(true);

        // when
        pullRequestOpenedService.createPullRequest(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestFileRepository.count()).isEqualTo(0),
                () -> assertThat(jpaCommitRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestContentHistoryRepository.count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestFileHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Pull_Request_생성_시_PullRequestOpenCreatedEvent가_발행된다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(false);

        // when
        pullRequestOpenedService.createPullRequest(TEST_API_KEY, request);

        // then
        long eventCount = applicationEvents.stream(PullRequestOpenCreatedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Draft_PR_생성_시_PullRequestDraftCreatedEvent가_발행된다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(true);

        // when
        pullRequestOpenedService.createPullRequest(TEST_API_KEY, request);

        // then
        long eventCount = applicationEvents.stream(PullRequestDraftCreatedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(false);
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> pullRequestOpenedService.createPullRequest(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class)
                .hasMessage("유효하지 않은 API Key입니다.");
    }

    private PullRequestOpenedRequest createPullRequestOpenedRequest(boolean isDraft) {
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

        return new PullRequestOpenedRequest(
                isDraft,
                pullRequestData,
                files
        );
    }
}
