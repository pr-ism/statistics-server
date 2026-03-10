package com.prism.statistics.application.statistics;

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
import com.prism.statistics.infrastructure.project.persistence.JpaProjectCoreTimeSettingRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewSpeedStatisticsQueryServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;
    private static final long REVIEWER_ID = 101L;
    private static final String REVIEWER_NAME = "reviewer";
    private static final String TEST_USER_NAME = "testuser";
    private static final String TEST_PROJECT_NAME = "Test Project";
    private static final String TEST_API_KEY_PREFIX = "test-api-key-";
    private static final String TEST_HEAD_COMMIT_SHA = "abc123";
    private static final String TEST_PR_TITLE = "Test PR";
    private static final String TEST_PR_LINK = "https://github.com/test/repo/pull/1";
    private static final String REVIEW_BODY = "LGTM";
    private static final int PR_NUMBER_BOUND = 10000;
    private static final int DEFAULT_COMMIT_COUNT = 4;
    private static final int DEFAULT_CHANGE_STATS_ADDITIONS = 2;
    private static final int DEFAULT_CHANGE_STATS_DELETIONS = 10;
    private static final int DEFAULT_CHANGE_STATS_CHANGED_FILES = 6;
    private static final int DEFAULT_DAYS_RANGE = 7;
    private static final int ONE_DAY = 1;
    private static final int CORE_TIME_START_HOUR = 9;
    private static final int CORE_TIME_END_HOUR = 18;
    private static final long SIXTY_MINUTES = 60L;
    private static final long ONE_HUNDRED_TWENTY_MINUTES = 120L;
    private static final long THIRTY_MINUTES = 30L;
    private static final long MERGE_WAIT_MINUTES = 60L;
    private static final long REVIEW_WAIT_INCREMENT = 10L;
    private static final int REVIEW_WAIT_LOOP_COUNT = 10;
    private static final long ZERO_COUNT = 0L;
    private static final long ONE_COUNT = 1L;
    private static final long TWO_COUNT = 2L;
    private static final int ZERO_INT = 0;
    private static final int ONE_INT = 1;

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
        Project project = createAndSaveProject(USER_ID);

        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReview(pr1);
        createAndSaveReview(pr2);

        createAndSaveBottleneck(pr1.getId(), SIXTY_MINUTES, true);
        createAndSaveBottleneck(pr2.getId(), ONE_HUNDRED_TWENTY_MINUTES, true);

        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(TWO_COUNT),
                () -> assertThat(response.reviewedPullRequestCount()).isGreaterThan(0),
                () -> assertThat(response.reviewRate()).isGreaterThan(0),
                () -> assertThat(response.reviewWaitTime().avgReviewWaitMinutes()).isGreaterThan(0)
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(USER_ID, project.getId(), request);

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
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr);
        createAndSaveBottleneck(pr.getId(), SIXTY_MINUTES, false);

        LocalDate startDate = LocalDate.now().minusDays(DEFAULT_DAYS_RANGE);
        LocalDate endDate = LocalDate.now().plusDays(ONE_DAY);
        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(startDate, endDate);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(USER_ID, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(ONE_COUNT);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(OTHER_USER_ID, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 코어타임_설정이_있으면_해당_설정을_사용한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        createAndSaveCoreTimeSetting(
                project.getId(),
                LocalTime.of(CORE_TIME_START_HOUR, ZERO_INT),
                LocalTime.of(CORE_TIME_END_HOUR, ZERO_INT)
        );

        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr);
        createAndSaveBottleneck(pr.getId(), SIXTY_MINUTES, false);

        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(USER_ID, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(ONE_COUNT);
    }

    @Test
    void 리뷰_대기_시간_백분위수를_계산한다() {
        // given
        Project project = createAndSaveProject(USER_ID);

        for (int i = 0; i < REVIEW_WAIT_LOOP_COUNT; i++) {
            PullRequest pr = createAndSavePullRequest(project.getId());
            createAndSaveReview(pr);
            createAndSaveBottleneck(pr.getId(), (i + 1) * REVIEW_WAIT_INCREMENT, false);
        }

        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(USER_ID, project.getId(), request);

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
        Project project = createAndSaveProject(USER_ID);

        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr);
        createAndSaveBottleneck(pr.getId(), SIXTY_MINUTES, false);

        ReviewSpeedStatisticsRequest request = new ReviewSpeedStatisticsRequest(null, null);

        // when
        ReviewSpeedStatisticsResponse response = reviewSpeedStatisticsQueryService
                .findReviewSpeedStatistics(USER_ID, project.getId(), request);

        // then
        assertThat(response.mergeWaitTime().avgMergeWaitMinutes()).isZero();
    }

    @Test
    void 전체_건수가_0이면_리뷰율은_0이다() {
        double result = ReflectionTestUtils.invokeMethod(
                reviewSpeedStatisticsQueryService,
                "calculatePercentage",
                ONE_COUNT,
                ZERO_COUNT
        );

        assertThat(result).isZero();
    }

    @Test
    void 평균_계산_시_건수가_0이면_0을_반환한다() {
        double result = ReflectionTestUtils.invokeMethod(
                reviewSpeedStatisticsQueryService,
                "calculateAverage",
                ONE_COUNT,
                ZERO_COUNT
        );

        assertThat(result).isZero();
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create(TEST_PROJECT_NAME, TEST_API_KEY_PREFIX + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create(TEST_USER_NAME, USER_ID))
                .pullRequestNumber((int) (Math.abs(System.nanoTime() % PR_NUMBER_BOUND)) + 1)
                .headCommitSha(TEST_HEAD_COMMIT_SHA)
                .title(TEST_PR_TITLE)
                .state(PullRequestState.OPEN)
                .link(TEST_PR_LINK)
                .changeStats(PullRequestChangeStats.create(
                        DEFAULT_CHANGE_STATS_ADDITIONS,
                        DEFAULT_CHANGE_STATS_DELETIONS,
                        DEFAULT_CHANGE_STATS_CHANGED_FILES
                ))
                .commitCount(DEFAULT_COMMIT_COUNT)
                .timing(PullRequestTiming.createOpen(createdAt))
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private void createAndSaveReview(PullRequest pullRequest) {
        Review review = Review.builder()
                .githubPullRequestId(pullRequest.getGithubPullRequestId())
                .pullRequestNumber(pullRequest.getPullRequestNumber())
                .githubReviewId(System.nanoTime())
                .reviewer(GithubUser.create(REVIEWER_NAME, REVIEWER_ID))
                .reviewState(ReviewState.APPROVED)
                .headCommitSha(TEST_HEAD_COMMIT_SHA)
                .body(REVIEW_BODY)
                .commentCount(ONE_INT)
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
                now,
                hasMergeWait
        );

        if (hasMergeWait) {
            bottleneck.updateOnNewReview(now.plusMinutes(THIRTY_MINUTES), true);
            bottleneck.updateOnMerge(now.plusMinutes(MERGE_WAIT_MINUTES));
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
