package com.prism.statistics.application.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestClosedRequest;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestStateChangedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestStateHistory;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestStateHistoryRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.time.LocalDateTime;

@IntegrationTest
@RecordApplicationEvents
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestClosedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_PULL_REQUEST_NUMBER = 123;
    private static final String TEST_HEAD_COMMIT_SHA = "abc123";
    private static final Instant CLOSED_AT = Instant.parse("2099-01-15T12:00:00Z");
    private static final Instant MERGED_AT = Instant.parse("2099-01-15T12:00:00Z");

    @Autowired
    private PullRequestClosedService pullRequestClosedService;

    @Autowired
    private JpaPullRequestRepository jpaPullRequestRepository;

    @Autowired
    private JpaPullRequestStateHistoryRepository jpaPullRequestStateHistoryRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Pull_Request_closed_이벤트를_처리하면_CLOSED_상태로_변경된다() {
        // given
        PullRequestClosedRequest request = createClosedRequest();

        // when
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getState()).isEqualTo(PullRequestState.CLOSED);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Pull_Request_merged_이벤트를_처리하면_MERGED_상태로_변경된다() {
        // given
        PullRequestClosedRequest request = createMergedRequest();

        // when
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getState()).isEqualTo(PullRequestState.MERGED);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Pull_Request_closed_이벤트를_처리하면_PullRequestStateChangedEvent가_발행된다() {
        // given
        PullRequestClosedRequest request = createClosedRequest();

        // when
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // then
        long eventCount = applicationEvents.stream(PullRequestStateChangedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Pull_Request_closed_이벤트를_처리하면_StateHistory가_저장된다() {
        // given
        PullRequestClosedRequest request = createClosedRequest();

        // when
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // then
        assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(1);
        PullRequestStateHistory history = jpaPullRequestStateHistoryRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(history.getPreviousState()).isEqualTo(PullRequestState.OPEN),
                () -> assertThat(history.getNewState()).isEqualTo(PullRequestState.CLOSED),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(TEST_HEAD_COMMIT_SHA)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Pull_Request_merged_이벤트를_처리하면_StateHistory가_저장된다() {
        // given
        PullRequestClosedRequest request = createMergedRequest();

        // when
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // then
        assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(1);
        PullRequestStateHistory history = jpaPullRequestStateHistoryRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(history.getPreviousState()).isEqualTo(PullRequestState.OPEN),
                () -> assertThat(history.getNewState()).isEqualTo(PullRequestState.MERGED),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(TEST_HEAD_COMMIT_SHA)
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void Pull_Request_merged_이벤트를_처리하면_StateHistory에_mergedAt이_기록된다() {
        // given
        PullRequestClosedRequest request = createMergedRequest();

        // when
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // then
        PullRequestStateHistory history = jpaPullRequestStateHistoryRepository.findAll().getFirst();
        LocalDateTime expectedMergedAt = LocalDateTime.of(2099, 1, 15, 21, 30, 0);
        assertThat(history.getGithubChangedAt()).isEqualTo(expectedMergedAt);
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void 이미_닫힌_PullRequest에_대해_closed_이벤트가_오면_상태가_변경되지_않고_이벤트가_발행되지_않는다() {
        // given
        PullRequestClosedRequest request = createClosedRequest();
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // when
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // then
        long eventCount = applicationEvents.stream(PullRequestStateChangedEvent.class).count();
        assertAll(
                () -> assertThat(jpaPullRequestRepository.findAll().getFirst().getState()).isEqualTo(PullRequestState.CLOSED),
                () -> assertThat(eventCount).isEqualTo(1),
                () -> assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(1)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PullRequest가_존재하지_않으면_아무_동작도_하지_않는다() {
        // given
        PullRequestClosedRequest request = createClosedRequest();

        // when
        pullRequestClosedService.closePullRequest(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(applicationEvents.stream(PullRequestStateChangedEvent.class).count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        PullRequestClosedRequest request = createClosedRequest();
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> pullRequestClosedService.closePullRequest(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    private PullRequestClosedRequest createClosedRequest() {
        return new PullRequestClosedRequest(
                TEST_PULL_REQUEST_NUMBER,
                false,
                CLOSED_AT,
                null
        );
    }

    private PullRequestClosedRequest createMergedRequest() {
        return new PullRequestClosedRequest(
                TEST_PULL_REQUEST_NUMBER,
                true,
                CLOSED_AT,
                MERGED_AT
        );
    }
}
