package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.ReviewQualityStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse;
import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.review.ReviewResponseTime;
import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewActivityRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewResponseTimeRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewSessionRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewQualityStatisticsQueryServiceTest {

    @Autowired
    private ReviewQualityStatisticsQueryService reviewQualityStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaReviewActivityRepository reviewActivityRepository;

    @Autowired
    private JpaReviewSessionRepository reviewSessionRepository;

    @Autowired
    private JpaReviewResponseTimeRepository reviewResponseTimeRepository;

    @Test
    void 프로젝트의_리뷰_품질_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReviewActivity(pr1.getId(), 2, 5, 100, 50, true, 15);
        createAndSaveReviewActivity(pr2.getId(), 3, 8, 80, 40, false, 5);

        createAndSaveReviewSession(pr1.getId(), 1L, "reviewer1", 60L, 2);
        createAndSaveReviewSession(pr1.getId(), 2L, "reviewer2", 45L, 1);
        createAndSaveReviewSession(pr2.getId(), 1L, "reviewer1", 30L, 3);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(2),
                () -> assertThat(response.reviewedPullRequestCount()).isEqualTo(2),
                () -> assertThat(response.reviewRate()).isEqualTo(100.0),
                () -> assertThat(response.reviewActivity().avgReviewRoundTrips()).isEqualTo(2.5),
                () -> assertThat(response.reviewActivity().avgCommentCount()).isEqualTo(6.5),
                () -> assertThat(response.reviewActivity().withAdditionalReviewersCount()).isEqualTo(1),
                () -> assertThat(response.reviewActivity().withChangesAfterReviewCount()).isEqualTo(1),
                () -> assertThat(response.reviewerStats().totalReviewerCount()).isEqualTo(2),
                () -> assertThat(response.reviewerStats().avgReviewersPerPr()).isEqualTo(1.0)
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReviewActivity(pr.getId(), 1, 3, 50, 30, false, 0);

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);
        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(startDate, endDate);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(userId, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(1);
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isZero(),
                () -> assertThat(response.reviewedPullRequestCount()).isZero(),
                () -> assertThat(response.reviewRate()).isZero(),
                () -> assertThat(response.reviewActivity().avgReviewRoundTrips()).isZero(),
                () -> assertThat(response.reviewerStats().totalReviewerCount()).isZero()
        );
    }

    @Test
    void 리뷰_활동만_있고_세션이_없는_경우_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReviewActivity(pr.getId(), 2, 4, 60, 20, true, 12);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(1),
                () -> assertThat(response.reviewActivity().avgReviewRoundTrips()).isEqualTo(2.0),
                () -> assertThat(response.reviewerStats().totalReviewerCount()).isZero()
        );
    }

    @Test
    void 세션만_있고_리뷰_활동이_없는_경우_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReviewSession(pr.getId(), 1L, "reviewer1", 30L, 1);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isZero(),
                () -> assertThat(response.reviewerStats().totalReviewerCount()).isEqualTo(1),
                () -> assertThat(response.reviewerStats().avgSessionDurationMinutes()).isEqualTo(30.0)
        );
    }

    @Test
    void 리뷰되지_않은_PR도_포함하여_리뷰율을_계산한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReviewActivity(pr1.getId(), 1, 3, 50, 30, false, 0);
        createAndSaveReviewActivityWithoutReview(pr2.getId(), 40, 20);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(2),
                () -> assertThat(response.reviewedPullRequestCount()).isEqualTo(1),
                () -> assertThat(response.reviewRate()).isEqualTo(50.0)
        );
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long otherUserId = 999L;
        Project project = createAndSaveProject(userId);
        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                reviewQualityStatisticsQueryService.findReviewQualityStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 변경_해결_건수가_있으면_평균_해결_시간을_계산한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());

        createAndSaveReviewActivity(pr.getId(), 2, 4, 60, 20, true, 12);

        LocalDateTime changesRequestedAt = LocalDateTime.now().minusHours(2);
        LocalDateTime approvedAt = LocalDateTime.now().minusHours(1);
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(pr.getId(), changesRequestedAt);
        responseTime.updateOnApproveAfterChanges(approvedAt);
        reviewResponseTimeRepository.save(responseTime);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(userId, project.getId(), request);

        // then
        assertThat(response.reviewActivity().avgChangesResolutionMinutes()).isGreaterThan(0);
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create("Test Project", "test-api-key-" + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId) {
        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create("testuser", 1L))
                .pullRequestNumber((int) (System.nanoTime() % 10000))
                .headCommitSha("abc123")
                .title("Test PR")
                .state(PullRequestState.MERGED)
                .link("https://github.com/test/repo/pull/1")
                .changeStats(PullRequestChangeStats.create(2, 10, 6))
                .commitCount(4)
                .timing(PullRequestTiming.createOpen(LocalDateTime.now().minusDays(1)))
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private void createAndSaveReviewActivity(
            Long pullRequestId,
            int reviewRoundTrips,
            int commentCount,
            int totalAdditions,
            int totalDeletions,
            boolean hasAdditionalReviewers,
            int codeChangesAfterReview
    ) {
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(pullRequestId)
                .reviewRoundTrips(reviewRoundTrips)
                .totalCommentCount(commentCount)
                .totalAdditions(totalAdditions)
                .totalDeletions(totalDeletions)
                .additionalReviewerCount(hasAdditionalReviewers ? 1 : 0)
                .codeAdditionsAfterReview(codeChangesAfterReview)
                .codeDeletionsAfterReview(0)
                .build();

        reviewActivityRepository.save(activity);
    }

    private void createAndSaveReviewActivityWithoutReview(
            Long pullRequestId,
            int totalAdditions,
            int totalDeletions
    ) {
        ReviewActivity activity = ReviewActivity.createWithoutReview(pullRequestId, totalAdditions, totalDeletions);
        reviewActivityRepository.save(activity);
    }

    private void createAndSaveReviewSession(
            Long pullRequestId,
            Long reviewerGithubId,
            String reviewerName,
            Long sessionDurationMinutes,
            int reviewCount
    ) {
        LocalDateTime firstActivityAt = LocalDateTime.now().minusHours(2);
        LocalDateTime lastActivityAt = firstActivityAt.plusMinutes(sessionDurationMinutes);

        ReviewSession session = ReviewSession.create(
                pullRequestId,
                GithubUser.create(reviewerName, reviewerGithubId),
                firstActivityAt
        );

        if (sessionDurationMinutes > 0) {
            session.updateOnReview(lastActivityAt, 0);
        }

        for (int i = 2; i < reviewCount; i++) {
            session.updateOnReview(lastActivityAt, 0);
        }

        reviewSessionRepository.save(session);
    }
}
