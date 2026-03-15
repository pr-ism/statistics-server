package com.prism.statistics.application.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.Author;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitData;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitNode;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.CommitsConnection;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.PullRequestData;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaCommitRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestContentHistoryRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestFileHistoryRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestFileRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestStateHistoryRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
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

    private static final Long TEST_PROJECT_ID = 1L;

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
        pullRequestOpenedService.createPullRequest(TEST_PROJECT_ID, request);

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
        pullRequestOpenedService.createPullRequest(TEST_PROJECT_ID, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getState()).isEqualTo(PullRequestState.OPEN);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Draft_Pull_Request_opened_이벤트를_처리하면_DRAFT_상태로_저장된다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(true);

        // when
        pullRequestOpenedService.createPullRequest(TEST_PROJECT_ID, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getState()).isEqualTo(PullRequestState.DRAFT);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Pull_Request_생성_시_PullRequestOpenCreatedEvent가_발행된다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(false);

        // when
        pullRequestOpenedService.createPullRequest(TEST_PROJECT_ID, request);

        // then
        long eventCount = applicationEvents.stream(PullRequestOpenCreatedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void Draft_Pull_Request_opened_이벤트를_처리하면_이벤트가_발행되고_모든_엔티티가_저장된다() {
        // given
        PullRequestOpenedRequest request = createPullRequestOpenedRequest(true);

        // when
        pullRequestOpenedService.createPullRequest(TEST_PROJECT_ID, request);

        // then
        long eventCount = applicationEvents.stream(PullRequestOpenCreatedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
        assertAll(
                () -> assertThat(jpaPullRequestRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestFileRepository.count()).isEqualTo(2),
                () -> assertThat(jpaCommitRepository.count()).isEqualTo(2),
                () -> assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestContentHistoryRepository.count()).isEqualTo(1),
                () -> assertThat(jpaPullRequestFileHistoryRepository.count()).isEqualTo(2)
        );
    }

    private PullRequestOpenedRequest createPullRequestOpenedRequest(boolean isDraft) {
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

        return new PullRequestOpenedRequest(
                null,
                isDraft,
                pullRequestData,
                files
        );
    }
}
