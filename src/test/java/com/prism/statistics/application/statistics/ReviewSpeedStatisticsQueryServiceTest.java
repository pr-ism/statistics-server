package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.ReviewSpeedStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse;
import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.setting.ProjectCoreTimeSetting;
import com.prism.statistics.domain.project.setting.vo.CoreTime;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestBottleneckRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectCoreTimeSettingRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewSpeedStatisticsQueryServiceTest {

    @Autowired
    private ReviewSpeedStatisticsQueryService reviewSpeedStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaReviewRepository reviewRepository;

    @Autowired
    private JpaPullRequestBottleneckRepository bottleneckRepository;

    @Autowired
    private JpaProjectCoreTimeSettingRepository coreTimeSettingRepository;

    @Test
    void 프로젝트의_리뷰_속도_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReview(pr1);
        createAndSaveReview(pr2);

        createAndSaveBottleneck(pr1.getId(), 60L, true);
        createAndSaveBottleneck(pr2.getId(), 120L, true);

        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(2),
                () -> assertThat(response.reviewedPullRequestCount()).isGreaterThan(0),
                () -> assertThat(response.reviewRate()).isGreaterThan(0),
                () -> assertThat(response.reviewWaitTime().avgReviewWaitMinutes()).isGreaterThan(0)
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isZero(),
                () -> assertThat(response.reviewedPullRequestCount()).isZero(),
                () -> assertThat(response.reviewRate()).isZero(),
                () -> assertThat(response.reviewWaitTime().avgReviewWaitMinutes()).isZero()
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr);
        createAndSaveBottleneck(pr.getId(), 60L, false);

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);
        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(startDate, endDate);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(userId, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(1);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long otherUserId = 999L;
        Project project = createAndSaveProject(userId);
        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 코어타임_설정이_있으면_해당_설정을_사용한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        createAndSaveCoreTimeSetting(project.getId(), LocalTime.of(9, 0), LocalTime.of(18, 0));

        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr);
        createAndSaveBottleneck(pr.getId(), 60L, false);

        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(userId, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(1);
    }

    @Test
    void 리뷰_대기_시간_백분위수를_계산한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        for (int i = 0; i < 10; i++) {
            PullRequest pr = createAndSavePullRequest(project.getId());
            createAndSaveReview(pr);
            createAndSaveBottleneck(pr.getId(), (i + 1) * 10L, false);
        }

        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.reviewWaitTime().reviewWaitP50Minutes()).isGreaterThan(0),
                () -> assertThat(response.reviewWaitTime().reviewWaitP90Minutes()).isGreaterThan(0),
                () -> assertThat(response.reviewWaitTime().reviewWaitP90Minutes())
                        .isGreaterThanOrEqualTo(response.reviewWaitTime().reviewWaitP50Minutes())
        );
    }

    @Test
    void 승인_후_머지된_PR이_없으면_머지_대기_시간은_0이다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr);
        createAndSaveBottleneck(pr.getId(), 60L, false);

        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(userId, project.getId(), request);

        // then
        assertThat(response.mergeWaitTime().avgMergeWaitMinutes()).isZero();
    }

    @Test
    void 전체_건수가_0이면_리뷰율은_0이다() {
        double result = ReflectionTestUtils.invokeMethod(
                reviewSpeedStatisticsQueryService,
                "calculatePercentage",
                1L,
                0L
        );

        assertThat(result).isZero();
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create("Test Project", "test-api-key-" + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create("testuser", 1L))
                .pullRequestNumber((int) (Math.abs(System.nanoTime() % 10000)))
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
                .githubReviewId(System.nanoTime())
                .reviewer(GithubUser.create("reviewer", 101L))
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(1)
                .githubSubmittedAt(LocalDateTime.now())
                .build();
        review.assignPullRequestId(pullRequest.getId());

        reviewRepository.save(review);
    }

    private void createAndSaveBottleneck(Long pullRequestId, Long reviewWaitMinutes, boolean hasMergeWait) {
        LocalDateTime now = LocalDateTime.now();
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                pullRequestId,
                now.minusMinutes(reviewWaitMinutes),
                now
        );

        if (hasMergeWait) {
            bottleneck.updateOnNewReview(now.plusMinutes(30), true);
            bottleneck.updateOnMerge(now.plusMinutes(60));
        }

        bottleneckRepository.save(bottleneck);
    }

    private void createAndSaveCoreTimeSetting(Long projectId, LocalTime startTime, LocalTime endTime) {
        ProjectCoreTimeSetting setting = ProjectCoreTimeSetting.create(
                projectId,
                CoreTime.of(startTime, endTime)
        );
        coreTimeSettingRepository.save(setting);
    }
}
