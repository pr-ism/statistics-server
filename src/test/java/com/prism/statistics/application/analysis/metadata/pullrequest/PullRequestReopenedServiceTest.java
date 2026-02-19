package com.prism.statistics.application.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestReopenedRequest;
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

@IntegrationTest
@RecordApplicationEvents
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestReopenedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final int TEST_PULL_REQUEST_NUMBER = 123;
    private static final String TEST_HEAD_COMMIT_SHA = "abc123";
    private static final Instant REOPENED_AT = Instant.parse("2099-01-15T12:00:00Z");

    @Autowired
    private PullRequestReopenedService pullRequestReopenedService;

    @Autowired
    private JpaPullRequestRepository jpaPullRequestRepository;

    @Autowired
    private JpaPullRequestStateHistoryRepository jpaPullRequestStateHistoryRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Sql("/sql/webhook/insert_project_and_closed_pull_request.sql")
    @Test
    void Pull_Request_reopened_이벤트를_처리하면_OPEN_상태로_변경된다() {
        // given
        PullRequestReopenedRequest request = createReopenedRequest();

        // when
        pullRequestReopenedService.reopenPullRequest(TEST_API_KEY, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getState()).isEqualTo(PullRequestState.OPEN);
    }

    @Sql("/sql/webhook/insert_project_and_closed_pull_request.sql")
    @Test
    void Pull_Request_reopened_이벤트를_처리하면_PullRequestStateChangedEvent가_발행된다() {
        // given
        PullRequestReopenedRequest request = createReopenedRequest();

        // when
        pullRequestReopenedService.reopenPullRequest(TEST_API_KEY, request);

        // then
        long eventCount = applicationEvents.stream(PullRequestStateChangedEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }

    @Sql("/sql/webhook/insert_project_and_closed_pull_request.sql")
    @Test
    void Pull_Request_reopened_이벤트를_처리하면_StateHistory가_저장된다() {
        // given
        PullRequestReopenedRequest request = createReopenedRequest();

        // when
        pullRequestReopenedService.reopenPullRequest(TEST_API_KEY, request);

        // then
        assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(1);
        PullRequestStateHistory history = jpaPullRequestStateHistoryRepository.findAll().getFirst();
        assertAll(
                () -> assertThat(history.getPreviousState()).isEqualTo(PullRequestState.CLOSED),
                () -> assertThat(history.getNewState()).isEqualTo(PullRequestState.OPEN),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(TEST_HEAD_COMMIT_SHA)
        );
    }

    @Sql("/sql/webhook/insert_project_and_closed_pull_request.sql")
    @Test
    void Pull_Request_reopened_이벤트를_처리하면_timing에서_closedAt이_초기화된다() {
        // given
        PullRequestReopenedRequest request = createReopenedRequest();

        // when
        pullRequestReopenedService.reopenPullRequest(TEST_API_KEY, request);

        // then
        PullRequest pullRequest = jpaPullRequestRepository.findAll().getFirst();
        assertThat(pullRequest.getTiming().getGithubClosedAt()).isNull();
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void CLOSED_상태가_아닌_PullRequest에_reopened_이벤트가_오면_상태가_변경되지_않고_이벤트가_발행되지_않는다() {
        // given
        PullRequestReopenedRequest request = createReopenedRequest();

        // when
        pullRequestReopenedService.reopenPullRequest(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(jpaPullRequestRepository.findAll().getFirst().getState()).isEqualTo(PullRequestState.OPEN),
                () -> assertThat(applicationEvents.stream(PullRequestStateChangedEvent.class).count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void PullRequest가_존재하지_않으면_아무_동작도_하지_않는다() {
        // given
        PullRequestReopenedRequest request = createReopenedRequest();

        // when
        pullRequestReopenedService.reopenPullRequest(TEST_API_KEY, request);

        // then
        assertAll(
                () -> assertThat(applicationEvents.stream(PullRequestStateChangedEvent.class).count()).isEqualTo(0),
                () -> assertThat(jpaPullRequestStateHistoryRepository.count()).isEqualTo(0)
        );
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        PullRequestReopenedRequest request = createReopenedRequest();
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> pullRequestReopenedService.reopenPullRequest(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    private PullRequestReopenedRequest createReopenedRequest() {
        return new PullRequestReopenedRequest(
                TEST_PULL_REQUEST_NUMBER,
                REOPENED_AT
        );
    }
}
