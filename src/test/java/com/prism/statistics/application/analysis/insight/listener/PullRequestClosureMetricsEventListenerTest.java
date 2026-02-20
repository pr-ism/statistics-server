package com.prism.statistics.application.analysis.insight.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestStateChangedEvent;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestLifecycleRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewActivityRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestClosureMetricsEventListenerTest {

    @Autowired
    private PullRequestClosureMetricsEventListener eventListener;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaPullRequestLifecycleRepository lifecycleRepository;

    @Autowired
    private JpaReviewActivityRepository reviewActivityRepository;

    @Test
    void MERGED_상태_이벤트를_처리하면_lifecycle과_reviewActivity가_저장된다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 16, 14, 30);
        PullRequest savedPullRequest = createAndSavePullRequest(createdAt);

        PullRequestStateChangedEvent event = new PullRequestStateChangedEvent(
                savedPullRequest.getId(),
                savedPullRequest.getHeadCommitSha(),
                PullRequestState.OPEN,
                PullRequestState.MERGED,
                mergedAt
        );

        // when
        eventListener.handle(event);

        // then
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                assertAll(
                        () -> assertThat(lifecycleRepository.count()).isEqualTo(1),
                        () -> assertThat(reviewActivityRepository.count()).isEqualTo(1)
                )
        );
    }

    @Test
    void CLOSED_상태_이벤트를_처리하면_lifecycle과_reviewActivity가_저장된다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 16, 10, 0);
        PullRequest savedPullRequest = createAndSavePullRequest(createdAt);

        PullRequestStateChangedEvent event = new PullRequestStateChangedEvent(
                savedPullRequest.getId(),
                savedPullRequest.getHeadCommitSha(),
                PullRequestState.OPEN,
                PullRequestState.CLOSED,
                closedAt
        );

        // when
        eventListener.handle(event);

        // then
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                assertAll(
                        () -> assertThat(lifecycleRepository.count()).isEqualTo(1),
                        () -> assertThat(reviewActivityRepository.count()).isEqualTo(1)
                )
        );
    }

    @Test
    void OPEN_상태_이벤트는_처리하지_않는다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime reopenedAt = LocalDateTime.of(2024, 1, 16, 10, 0);
        PullRequest savedPullRequest = createAndSavePullRequest(createdAt);

        PullRequestStateChangedEvent event = new PullRequestStateChangedEvent(
                savedPullRequest.getId(),
                savedPullRequest.getHeadCommitSha(),
                PullRequestState.CLOSED,
                PullRequestState.OPEN,
                reopenedAt
        );

        // when
        eventListener.handle(event);

        // then
        assertAll(
                () -> assertThat(lifecycleRepository.count()).isEqualTo(0),
                () -> assertThat(reviewActivityRepository.count()).isEqualTo(0)
        );
    }

    private PullRequest createAndSavePullRequest(LocalDateTime createdAt) {
        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(12345L)
                .projectId(10L)
                .author(GithubUser.create("testuser", 1L))
                .pullRequestNumber(1)
                .headCommitSha("abc123")
                .title("Test PR")
                .state(PullRequestState.OPEN)
                .link("https://github.com/test/repo/pull/1")
                .changeStats(PullRequestChangeStats.create(2, 10, 6))
                .commitCount(4)
                .timing(PullRequestTiming.createOpen(createdAt))
                .build();

        return pullRequestRepository.save(pullRequest);
    }
}
