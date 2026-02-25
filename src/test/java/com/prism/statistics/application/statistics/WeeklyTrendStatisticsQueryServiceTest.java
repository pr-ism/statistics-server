package com.prism.statistics.application.statistics;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.WeeklyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse;
import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestBottleneckRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestSizeRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WeeklyTrendStatisticsQueryServiceTest {

    private static final long DEFAULT_USER_ID = 1L;
    private static final long OTHER_USER_ID = 999L;
    private static final long AUTHOR_USER_ID = 1L;
    private static final int DAYS_2 = 2;
    private static final int DAYS_8 = 8;
    private static final int DAYS_1 = 1;
    private static final int WEEKS_2 = 2;
    private static final int TOTAL_CHANGES_75 = 75;
    private static final int TOTAL_CHANGES_90 = 90;
    private static final int FILE_COUNT_5 = 5;
    private static final long REVIEW_WAIT_MINUTES_120 = 120L;
    private static final int HALF_DIVISOR = 2;
    private static final String TEST_PROJECT_NAME = "Test Project";
    private static final String TEST_API_KEY_PREFIX = "test-api-key-";
    private static final String TEST_USER_NAME = "testuser";
    private static final String TEST_PULL_REQUEST_TITLE = "Test PR";
    private static final String TEST_PULL_REQUEST_URL = "https://github.com/test/repo/pull/1";
    private static final String TEST_HEAD_SHA = "abc123";
    private static final int CHANGE_STATS_ADDITIONS = 2;
    private static final int CHANGE_STATS_DELETIONS = 10;
    private static final int CHANGE_STATS_FILES = 6;
    private static final int COMMIT_COUNT = 4;
    private static final int PULL_REQUEST_NUMBER_MODULUS = 10000;

    @Autowired
    private WeeklyTrendStatisticsQueryService weeklyTrendStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaPullRequestSizeRepository pullRequestSizeRepository;

    @Autowired
    private JpaPullRequestBottleneckRepository bottleneckRepository;

    @Test
    void 주간_트렌드_통계를_조회한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);

        LocalDateTime now = LocalDateTime.now();
        PullRequest pr1 = createAndSavePullRequest(project.getId(), now.minusDays(DAYS_8), now.minusDays(DAYS_1)); // 지난주 생성 및 머지
        PullRequest pr2 = createAndSavePullRequest(project.getId(), now.minusDays(DAYS_2), null); // 이번주 생성

        createAndSavePullRequestSize(pr1.getId(), TOTAL_CHANGES_75, FILE_COUNT_5);
        createAndSavePullRequestSize(pr2.getId(), TOTAL_CHANGES_90, FILE_COUNT_5);
        createAndSaveBottleneck(pr1.getId(), REVIEW_WAIT_MINUTES_120); // 리뷰 대기 시간 데이터 추가

        WeeklyTrendStatisticsRequest request = new WeeklyTrendStatisticsRequest(
                LocalDate.now().minusWeeks(WEEKS_2),
                LocalDate.now()
        );

        // when
        WeeklyTrendStatisticsResponse response = weeklyTrendStatisticsQueryService
                .findWeeklyTrendStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.weeklyThroughput()).isNotEmpty(),
                () -> assertThat(response.monthlyThroughput()).isNotEmpty(),
                () -> assertThat(response.weeklyReviewWaitTimeTrend()).isNotEmpty(),
                () -> assertThat(response.weeklyPrSizeTrend()).isNotEmpty()
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);
        WeeklyTrendStatisticsRequest request = new WeeklyTrendStatisticsRequest(null, null);

        // when
        WeeklyTrendStatisticsResponse response = weeklyTrendStatisticsQueryService
                .findWeeklyTrendStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.weeklyThroughput()).isEmpty(),
                () -> assertThat(response.monthlyThroughput()).isEmpty(),
                () -> assertThat(response.weeklyReviewWaitTimeTrend()).isEmpty(),
                () -> assertThat(response.weeklyPrSizeTrend()).isEmpty()
        );
    }

    @Test
    void 소유하지_않은_프로젝트는_예외가_발생한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Long otherUserId = OTHER_USER_ID;
        Project project = createAndSaveProject(userId);
        WeeklyTrendStatisticsRequest request = new WeeklyTrendStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }


    private Project createAndSaveProject(Long userId) {
        Project project = Project.create(TEST_PROJECT_NAME, TEST_API_KEY_PREFIX + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId, LocalDateTime createdAt, LocalDateTime mergedAt) {
        PullRequestState state = resolveState(mergedAt);
        PullRequestTiming timing = createTiming(createdAt, mergedAt);

        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create(TEST_USER_NAME, AUTHOR_USER_ID))
                .pullRequestNumber((int) (Math.abs(System.nanoTime() % PULL_REQUEST_NUMBER_MODULUS)))
                .headCommitSha(TEST_HEAD_SHA)
                .title(TEST_PULL_REQUEST_TITLE)
                .state(state)
                .link(TEST_PULL_REQUEST_URL)
                .changeStats(PullRequestChangeStats.create(CHANGE_STATS_ADDITIONS, CHANGE_STATS_DELETIONS, CHANGE_STATS_FILES))
                .commitCount(COMMIT_COUNT)
                .timing(timing)
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private void createAndSavePullRequestSize(Long pullRequestId, int totalChanges, int fileCount) {
        int additions = totalChanges / HALF_DIVISOR;
        int deletions = totalChanges - additions;

        PullRequestSize prSize = PullRequestSize.create(
                pullRequestId,
                additions,
                deletions,
                fileCount,
                java.math.BigDecimal.ZERO
        );
        pullRequestSizeRepository.save(prSize);
    }

    private PullRequestState resolveState(LocalDateTime mergedAt) {
        if (mergedAt == null) {
            return PullRequestState.OPEN;
        }
        return PullRequestState.MERGED;
    }

    private PullRequestTiming createTiming(LocalDateTime createdAt, LocalDateTime mergedAt) {
        if (mergedAt == null) {
            return PullRequestTiming.createOpen(createdAt);
        }
        return PullRequestTiming.createMerged(createdAt, mergedAt);
    }

    private void createAndSaveBottleneck(Long pullRequestId, Long reviewWaitMinutes) {
        LocalDateTime now = LocalDateTime.now();
        com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck bottleneck = com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck.createOnFirstReview(
                pullRequestId,
                now.minusMinutes(reviewWaitMinutes),
                now,
                false
        );
        bottleneckRepository.save(bottleneck);
    }
}
