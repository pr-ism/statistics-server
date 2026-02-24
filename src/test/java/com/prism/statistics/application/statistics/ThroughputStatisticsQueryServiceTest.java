package com.prism.statistics.application.statistics;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.ThroughputStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ThroughputStatisticsResponse;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
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
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertAll;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ThroughputStatisticsQueryServiceTest {

    private static final long DEFAULT_USER_ID = 1L;
    private static final long OTHER_USER_ID = 999L;
    private static final long MERGE_TIME_MINUTES_1440 = 1440L;
    private static final long MERGE_TIME_MINUTES_2880 = 2880L;
    private static final long PULL_REQUEST_COUNT_ONE = 1L;
    private static final long PULL_REQUEST_COUNT_TWO = 2L;
    private static final long CLOSED_PR_COUNT_ONE = 1L;
    private static final long COUNT_ZERO = 0L;
    private static final double ZERO_DOUBLE = 0.0;
    private static final double AVG_MERGE_TIME_2160 = 2160.0;
    private static final double MERGE_SUCCESS_RATE_66_67 = 66.67;
    private static final double CLOSED_PR_RATE_33_33 = 33.33;
    private static final double MERGE_SUCCESS_RATE_100 = 100.0;
    private static final double CLOSED_PR_RATE_100 = 100.0;
    private static final double DELTA_1 = 1.0;
    private static final double DELTA_0_01 = 0.01;
    private static final long DATE_RANGE_DAYS = 7L;
    private static final long DATE_RANGE_EXTRA_DAYS = 1L;
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
    private static final int PULL_REQUEST_CREATED_DAYS_AGO = 1;
    private static final long DEFAULT_AUTHOR_ID = 1L;

    @Autowired
    private ThroughputStatisticsQueryService throughputStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Test
    void 프로젝트의_처리량_통계를_조회한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);

        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, MERGE_TIME_MINUTES_1440);
        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, MERGE_TIME_MINUTES_2880);
        createAndSavePullRequest(project.getId(), PullRequestState.CLOSED, null);

        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(null, null);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.mergedPrCount()).isEqualTo(PULL_REQUEST_COUNT_TWO),
                () -> assertThat(response.closedPrCount()).isEqualTo(CLOSED_PR_COUNT_ONE),
                () -> assertThat(response.avgMergeTimeMinutes()).isCloseTo(AVG_MERGE_TIME_2160, within(DELTA_1)),
                () -> assertThat(response.mergeSuccessRate()).isCloseTo(MERGE_SUCCESS_RATE_66_67, within(DELTA_0_01)),
                () -> assertThat(response.closedPrRate()).isCloseTo(CLOSED_PR_RATE_33_33, within(DELTA_0_01))
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);
        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(null, null);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.mergedPrCount()).isZero(),
                () -> assertThat(response.closedPrCount()).isZero(),
                () -> assertThat(response.avgMergeTimeMinutes()).isZero(),
                () -> assertThat(response.mergeSuccessRate()).isZero(),
                () -> assertThat(response.closedPrRate()).isZero()
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);
        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, MERGE_TIME_MINUTES_1440);

        LocalDate startDate = LocalDate.now().minusDays(DATE_RANGE_DAYS);
        LocalDate endDate = LocalDate.now().plusDays(DATE_RANGE_EXTRA_DAYS);
        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(startDate, endDate);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertThat(response.mergedPrCount()).isEqualTo(PULL_REQUEST_COUNT_ONE);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Long otherUserId = OTHER_USER_ID;
        Project project = createAndSaveProject(userId);
        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                throughputStatisticsQueryService.findThroughputStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 머지된_PR이_없으면_평균_머지_시간은_0이다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);
        createAndSavePullRequest(project.getId(), PullRequestState.CLOSED, null);

        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(null, null);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.mergedPrCount()).isZero(),
                () -> assertThat(response.closedPrCount()).isEqualTo(CLOSED_PR_COUNT_ONE),
                () -> assertThat(response.avgMergeTimeMinutes()).isZero(),
                () -> assertThat(response.mergeSuccessRate()).isZero(),
                () -> assertThat(response.closedPrRate()).isEqualTo(CLOSED_PR_RATE_100)
        );
    }

    @Test
    void 모든_PR이_머지되면_머지_성공률은_100이다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);
        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, MERGE_TIME_MINUTES_1440);
        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, MERGE_TIME_MINUTES_2880);

        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(null, null);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.mergedPrCount()).isEqualTo(PULL_REQUEST_COUNT_TWO),
                () -> assertThat(response.closedPrCount()).isZero(),
                () -> assertThat(response.mergeSuccessRate()).isEqualTo(MERGE_SUCCESS_RATE_100),
                () -> assertThat(response.closedPrRate()).isZero()
        );
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create(TEST_PROJECT_NAME, TEST_API_KEY_PREFIX + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId, PullRequestState state, Long mergeTimeMinutes) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(PULL_REQUEST_CREATED_DAYS_AGO);
        LocalDateTime closedAt = LocalDateTime.now();

        PullRequestTiming timing = createTiming(state, mergeTimeMinutes, createdAt, closedAt);

        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create(TEST_USER_NAME, DEFAULT_AUTHOR_ID))
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

    private PullRequestTiming createTiming(
            PullRequestState state,
            Long mergeTimeMinutes,
            LocalDateTime createdAt,
            LocalDateTime closedAt
    ) {
        if (state.isMerged()) {
            return PullRequestTiming.createMerged(closedAt.minusMinutes(mergeTimeMinutes), closedAt);
        }
        return PullRequestTiming.createClosed(createdAt, closedAt);
    }
}
