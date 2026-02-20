package com.prism.statistics.application.analysis.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestLifecycleRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewActivityRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestClosureMetricsServiceTest {

    @Autowired
    private PullRequestClosureMetricsService closureMetricsService;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaReviewRepository reviewRepository;

    @Autowired
    private JpaPullRequestLifecycleRepository lifecycleRepository;

    @Autowired
    private JpaReviewActivityRepository reviewActivityRepository;

    @Test
    void PR이_머지되면_lifecycle과_reviewActivity가_저장된다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 16, 14, 30);
        PullRequest savedPullRequest = createAndSavePullRequest(createdAt);
        createAndSaveReview(savedPullRequest);

        // when
        closureMetricsService.deriveClosureMetrics(
                savedPullRequest.getId(),
                PullRequestState.MERGED,
                mergedAt
        );

        // then
        List<PullRequestLifecycle> lifecycles = lifecycleRepository.findAll();
        List<ReviewActivity> activities = reviewActivityRepository.findAll();

        assertAll(
                () -> assertThat(lifecycles)
                        .singleElement()
                        .satisfies(lifecycle -> assertAll(
                                () -> assertThat(lifecycle.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                                () -> assertThat(lifecycle.isMerged()).isTrue(),
                                () -> assertThat(lifecycle.isClosed()).isTrue(),
                                () -> assertThat(lifecycle.isClosedWithoutReview()).isFalse(),
                                () -> assertThat(lifecycle.getTimeToMerge().getMinutes()).isEqualTo(1710L)
                        )),
                () -> assertThat(activities)
                        .singleElement()
                        .satisfies(activity -> assertAll(
                                () -> assertThat(activity.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                                () -> assertThat(activity.getReviewRoundTrips()).isEqualTo(1),
                                () -> assertThat(activity.getTotalCommentCount()).isEqualTo(3)
                        ))
        );
    }

    @Test
    void 리뷰_없이_종료되면_closedWithoutReview가_true이다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 16, 10, 0);
        PullRequest savedPullRequest = createAndSavePullRequest(createdAt);

        // when
        closureMetricsService.deriveClosureMetrics(
                savedPullRequest.getId(),
                PullRequestState.CLOSED,
                closedAt
        );

        // then
        List<PullRequestLifecycle> lifecycles = lifecycleRepository.findAll();
        List<ReviewActivity> activities = reviewActivityRepository.findAll();

        assertAll(
                () -> assertThat(lifecycles)
                        .singleElement()
                        .satisfies(lifecycle -> assertAll(
                                () -> assertThat(lifecycle.isMerged()).isFalse(),
                                () -> assertThat(lifecycle.isClosed()).isTrue(),
                                () -> assertThat(lifecycle.isClosedWithoutReview()).isTrue(),
                                () -> assertThat(lifecycle.getTotalLifespan().getMinutes()).isEqualTo(1440L)
                        )),
                () -> assertThat(activities)
                        .singleElement()
                        .satisfies(activity -> assertAll(
                                () -> assertThat(activity.getReviewRoundTrips()).isEqualTo(0),
                                () -> assertThat(activity.hasReviewActivity()).isFalse()
                        ))
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

    private void createAndSaveReview(PullRequest pullRequest) {
        Review review = Review.builder()
                .githubPullRequestId(pullRequest.getGithubPullRequestId())
                .pullRequestNumber(pullRequest.getPullRequestNumber())
                .githubReviewId(99999L)
                .reviewer(GithubUser.create("reviewer", 2L))
                .reviewState(ReviewState.APPROVED)
                .headCommitSha(pullRequest.getHeadCommitSha())
                .body("LGTM")
                .commentCount(3)
                .githubSubmittedAt(LocalDateTime.of(2024, 1, 16, 10, 0))
                .build();

        review.assignPullRequestId(pullRequest.getId());
        reviewRepository.save(review);
    }
}
