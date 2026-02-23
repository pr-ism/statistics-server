package com.prism.statistics.application.analysis.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.domain.analysis.insight.review.ReviewResponseTime;
import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.analysis.metadata.review.exception.ReviewNotFoundException;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewResponseTimeRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewSessionRepository;
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
class ReviewActivityMetricsServiceTest {

    @Autowired
    private ReviewActivityMetricsService reviewActivityMetricsService;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaReviewRepository reviewRepository;

    @Autowired
    private JpaReviewSessionRepository reviewSessionRepository;

    @Autowired
    private JpaReviewResponseTimeRepository reviewResponseTimeRepository;

    @Test
    void APPROVED_리뷰가_제출되면_ReviewSession이_생성된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        Review savedReview = createAndSaveReview(savedPullRequest, ReviewState.APPROVED, 3);

        // when
        reviewActivityMetricsService.deriveMetrics(savedReview.getGithubReviewId());

        // then
        List<ReviewSession> sessions = reviewSessionRepository.findAll();
        assertThat(sessions)
                .singleElement()
                .satisfies(session -> assertAll(
                        () -> assertThat(session.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                        () -> assertThat(session.getReviewer().getUserId()).isEqualTo(2L),
                        () -> assertThat(session.getReviewCount()).isEqualTo(1),
                        () -> assertThat(session.isActiveReviewer()).isTrue()
                ));
    }

    @Test
    void CHANGES_REQUESTED_리뷰가_제출되면_ReviewResponseTime이_생성된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        LocalDateTime changesRequestedAt = LocalDateTime.of(2024, 1, 16, 10, 0);
        Review savedReview = createAndSaveReview(savedPullRequest, ReviewState.CHANGES_REQUESTED, 2, changesRequestedAt);

        // when
        reviewActivityMetricsService.deriveMetrics(savedReview.getGithubReviewId());

        // then
        List<ReviewResponseTime> responseTimes = reviewResponseTimeRepository.findAll();
        assertThat(responseTimes)
                .singleElement()
                .satisfies(responseTime -> assertAll(
                        () -> assertThat(responseTime.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                        () -> assertThat(responseTime.hasChangesRequested()).isTrue(),
                        () -> assertThat(responseTime.getChangesRequestedCount()).isEqualTo(1),
                        () -> assertThat(responseTime.getLastChangesRequestedAt()).isEqualTo(changesRequestedAt)
                ));
    }

    @Test
    void CHANGES_REQUESTED_후_APPROVED가_제출되면_changesResolution이_기록된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        LocalDateTime changesRequestedAt = LocalDateTime.of(2024, 1, 16, 10, 0);
        LocalDateTime approvedAt = LocalDateTime.of(2024, 1, 17, 14, 30);

        Review changesRequestedReview = createAndSaveReview(
                savedPullRequest, ReviewState.CHANGES_REQUESTED, 2, changesRequestedAt
        );
        reviewActivityMetricsService.deriveMetrics(changesRequestedReview.getGithubReviewId());

        Review approvedReview = createAndSaveReview(
                savedPullRequest, ReviewState.APPROVED, 0, approvedAt, 88888L
        );

        // when
        reviewActivityMetricsService.deriveMetrics(approvedReview.getGithubReviewId());

        // then
        List<ReviewResponseTime> responseTimes = reviewResponseTimeRepository.findAll();
        assertThat(responseTimes)
                .singleElement()
                .satisfies(responseTime -> assertAll(
                        () -> assertThat(responseTime.isResolved()).isTrue(),
                        () -> assertThat(responseTime.getFirstApproveAfterChangesAt()).isEqualTo(approvedAt),
                        () -> assertThat(responseTime.getChangesResolution().getMinutes()).isEqualTo(1710L)
                ));
    }

    @Test
    void deriveMetricsByGithubReviewId로_리뷰_메트릭을_생성한다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        Review savedReview = createAndSaveReview(savedPullRequest, ReviewState.APPROVED, 3);

        // when
        reviewActivityMetricsService.deriveMetricsByGithubReviewId(savedReview.getGithubReviewId());

        // then
        List<ReviewSession> sessions = reviewSessionRepository.findAll();
        assertThat(sessions)
                .singleElement()
                .satisfies(session -> assertAll(
                        () -> assertThat(session.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                        () -> assertThat(session.getReviewer().getUserId()).isEqualTo(2L),
                        () -> assertThat(session.getReviewCount()).isEqualTo(1)
                ));
    }

    @Test
    void deriveMetricsByGithubReviewId에서_pullRequestId가_없으면_처리하지_않는다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        Review reviewWithoutPrId = Review.builder()
                .githubPullRequestId(savedPullRequest.getGithubPullRequestId())
                .pullRequestNumber(savedPullRequest.getPullRequestNumber())
                .githubReviewId(77777L)
                .reviewer(GithubUser.create("reviewer", 2L))
                .reviewState(ReviewState.APPROVED)
                .headCommitSha(savedPullRequest.getHeadCommitSha())
                .body("LGTM")
                .commentCount(2)
                .githubSubmittedAt(LocalDateTime.of(2024, 1, 16, 10, 0))
                .build();
        reviewRepository.save(reviewWithoutPrId);

        // when
        reviewActivityMetricsService.deriveMetricsByGithubReviewId(reviewWithoutPrId.getGithubReviewId());

        // then
        List<ReviewSession> sessions = reviewSessionRepository.findAll();
        assertThat(sessions).isEmpty();
    }

    @Test
    void deriveMetricsByGithubReviewId에서_존재하지_않는_리뷰는_무시한다() {
        // when
        reviewActivityMetricsService.deriveMetricsByGithubReviewId(999999L);

        // then
        List<ReviewSession> sessions = reviewSessionRepository.findAll();
        assertThat(sessions).isEmpty();
    }

    @Test
    void 동일_리뷰어가_여러번_리뷰하면_ReviewSession이_업데이트된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        LocalDateTime firstReviewAt = LocalDateTime.of(2024, 1, 16, 10, 0);
        LocalDateTime secondReviewAt = LocalDateTime.of(2024, 1, 17, 14, 30);

        Review firstReview = createAndSaveReview(savedPullRequest, ReviewState.COMMENTED, 1, firstReviewAt);
        reviewActivityMetricsService.deriveMetrics(firstReview.getGithubReviewId());

        Review secondReview = createAndSaveReview(savedPullRequest, ReviewState.APPROVED, 2, secondReviewAt, 88888L);

        // when
        reviewActivityMetricsService.deriveMetrics(secondReview.getGithubReviewId());

        // then
        List<ReviewSession> sessions = reviewSessionRepository.findAll();
        assertThat(sessions)
                .singleElement()
                .satisfies(session -> assertAll(
                        () -> assertThat(session.getReviewCount()).isEqualTo(2),
                        () -> assertThat(session.getFirstActivityAt()).isEqualTo(firstReviewAt),
                        () -> assertThat(session.getLastActivityAt()).isEqualTo(secondReviewAt),
                        () -> assertThat(session.getSessionDuration().getMinutes()).isEqualTo(1710L)
                ));
    }

    @Test
    void 존재하지_않는_리뷰로_메트릭을_생성하면_예외가_발생한다() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                reviewActivityMetricsService.deriveMetrics(999999L)
        ).isInstanceOf(ReviewNotFoundException.class)
                .hasMessageContaining("Review not found");
    }

    @Test
    void CHANGES_REQUESTED가_처음_발생하면_ReviewResponseTime이_새로_생성된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        LocalDateTime changesRequestedAt = LocalDateTime.of(2024, 1, 16, 10, 0);
        Review savedReview = createAndSaveReview(savedPullRequest, ReviewState.CHANGES_REQUESTED, 2, changesRequestedAt);

        // when
        reviewActivityMetricsService.deriveMetrics(savedReview.getGithubReviewId());

        // then
        List<ReviewResponseTime> responseTimes = reviewResponseTimeRepository.findAll();
        assertThat(responseTimes)
                .singleElement()
                .satisfies(responseTime -> assertAll(
                        () -> assertThat(responseTime.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                        () -> assertThat(responseTime.hasChangesRequested()).isTrue()
                ));
    }

    @Test
    void 기존_ReviewResponseTime이_있으면_CHANGES_REQUESTED시_업데이트된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        LocalDateTime firstChangesRequestedAt = LocalDateTime.of(2024, 1, 16, 10, 0);
        LocalDateTime secondChangesRequestedAt = LocalDateTime.of(2024, 1, 17, 10, 0);

        Review firstReview = createAndSaveReview(savedPullRequest, ReviewState.CHANGES_REQUESTED, 2, firstChangesRequestedAt);
        reviewActivityMetricsService.deriveMetrics(firstReview.getGithubReviewId());

        Review secondReview = createAndSaveReview(savedPullRequest, ReviewState.CHANGES_REQUESTED, 1, secondChangesRequestedAt, 77777L);

        // when
        reviewActivityMetricsService.deriveMetrics(secondReview.getGithubReviewId());

        // then
        List<ReviewResponseTime> responseTimes = reviewResponseTimeRepository.findAll();
        assertThat(responseTimes)
                .singleElement()
                .satisfies(responseTime -> assertAll(
                        () -> assertThat(responseTime.getChangesRequestedCount()).isEqualTo(2),
                        () -> assertThat(responseTime.getLastChangesRequestedAt()).isEqualTo(secondChangesRequestedAt)
                ));
    }

    @Test
    void 처음_리뷰하는_리뷰어면_새_ReviewSession이_생성된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        Review review = createAndSaveReview(savedPullRequest, ReviewState.COMMENTED, 1);

        // when
        reviewActivityMetricsService.deriveMetrics(review.getGithubReviewId());

        // then
        List<ReviewSession> sessions = reviewSessionRepository.findAll();
        assertThat(sessions)
                .singleElement()
                .satisfies(session -> assertAll(
                        () -> assertThat(session.getReviewCount()).isEqualTo(1),
                        () -> assertThat(session.getReviewer().getUserId()).isEqualTo(2L)
                ));
    }

    private PullRequest createAndSavePullRequest() {
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
                .timing(PullRequestTiming.createOpen(LocalDateTime.of(2024, 1, 15, 10, 0)))
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private Review createAndSaveReview(PullRequest pullRequest, ReviewState state, int commentCount) {
        return createAndSaveReview(pullRequest, state, commentCount, LocalDateTime.of(2024, 1, 16, 10, 0), 99999L);
    }

    private Review createAndSaveReview(
            PullRequest pullRequest,
            ReviewState state,
            int commentCount,
            LocalDateTime submittedAt
    ) {
        return createAndSaveReview(pullRequest, state, commentCount, submittedAt, 99999L);
    }

    private Review createAndSaveReview(
            PullRequest pullRequest,
            ReviewState state,
            int commentCount,
            LocalDateTime submittedAt,
            Long githubReviewId
    ) {
        Review review = Review.builder()
                .githubPullRequestId(pullRequest.getGithubPullRequestId())
                .pullRequestNumber(pullRequest.getPullRequestNumber())
                .githubReviewId(githubReviewId)
                .reviewer(GithubUser.create("reviewer", 2L))
                .reviewState(state)
                .headCommitSha(pullRequest.getHeadCommitSha())
                .body(state == ReviewState.COMMENTED ? "Comment body" : "LGTM")
                .commentCount(commentCount)
                .githubSubmittedAt(submittedAt)
                .build();

        review.assignPullRequestId(pullRequest.getId());
        return reviewRepository.save(review);
    }
}
