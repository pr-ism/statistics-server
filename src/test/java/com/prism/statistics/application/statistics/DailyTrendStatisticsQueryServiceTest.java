package com.prism.statistics.application.statistics;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.DailyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DailyTrendStatisticsQueryServiceTest {

    private static final long DEFAULT_USER_ID = 1L;
    private static final long OTHER_USER_ID = 999L;
    private static final long AUTHOR_USER_ID = 1L;
    private static final int DAYS_1 = 1;
    private static final int DAYS_2 = 2;
    private static final int DAYS_3 = 3;
    private static final int DATE_RANGE_DAYS = 3;
    private static final int DATE_RANGE_OVERLAP_DAYS = 1;
    private static final long ZERO_DAYS = 0L;
    private static final long TOTAL_CREATED_COUNT = 3L;
    private static final long TOTAL_MERGED_COUNT = 2L;
    private static final long ZERO_COUNT = 0L;
    private static final double ZERO_DOUBLE = 0.0;
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
    private static final long COUNT_ONE = 1L;
    private static final long COUNT_TWO = 2L;
    private static final long COUNT_THREE = 3L;
    private static final long DATE_RANGE_TWO_DAYS = 2L;

    @Autowired
    private DailyTrendStatisticsQueryService dailyTrendStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Test
    void 일별_트렌드_통계를_조회한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);

        LocalDateTime today = LocalDate.now().atStartOfDay();
        createAndSavePullRequest(project.getId(), today.minusDays(DAYS_1), today); // 어제 생성, 오늘 머지
        createAndSavePullRequest(project.getId(), today.minusDays(DAYS_2), today.minusDays(DAYS_1)); // 그제 생성, 어제 머지
        createAndSavePullRequest(project.getId(), today.minusDays(DAYS_3), null); // 3일 전 생성, 머지 안됨

        DailyTrendStatisticsRequest request = new DailyTrendStatisticsRequest(
                LocalDate.now().minusDays(DATE_RANGE_DAYS),
                LocalDate.now()
        );

        // when
        DailyTrendStatisticsResponse response = dailyTrendStatisticsQueryService
                .findDailyTrendStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.dailyCreatedTrend()).isNotEmpty(),
                () -> assertThat(response.dailyMergedTrend()).isNotEmpty(),
                () -> assertThat(response.summary().totalCreatedCount()).isEqualTo(TOTAL_CREATED_COUNT),
                () -> assertThat(response.summary().totalMergedCount()).isEqualTo(TOTAL_MERGED_COUNT),
                () -> assertThat(response.summary().avgDailyCreatedCount()).isGreaterThan(ZERO_DOUBLE),
                () -> assertThat(response.summary().avgDailyMergedCount()).isGreaterThan(ZERO_DOUBLE)
        );
    }

    @Test
    void 데이터가_없는_경우_빈_통계를_반환한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);
        DailyTrendStatisticsRequest request = new DailyTrendStatisticsRequest(
                LocalDate.now().minusDays(DATE_RANGE_DAYS),
                LocalDate.now()
        );

        // when
        DailyTrendStatisticsResponse response = dailyTrendStatisticsQueryService
                .findDailyTrendStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.dailyCreatedTrend()).isEmpty(),
                () -> assertThat(response.dailyMergedTrend()).isEmpty(),
                () -> assertThat(response.summary().totalCreatedCount()).isEqualTo(ZERO_COUNT),
                () -> assertThat(response.summary().totalMergedCount()).isEqualTo(ZERO_COUNT)
        );
    }

    @Test
    void 소유하지_않은_프로젝트_조회시_예외가_발생한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Long otherUserId = OTHER_USER_ID;
        Project project = createAndSaveProject(userId);
        DailyTrendStatisticsRequest request = new DailyTrendStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                dailyTrendStatisticsQueryService.findDailyTrendStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 평균_계산시_일수가_0이면_0을_반환한다() {
        double result = ReflectionTestUtils.invokeMethod(
                dailyTrendStatisticsQueryService,
                "calculateAverage",
                TOTAL_CREATED_COUNT,
                ZERO_DAYS
        );

        assertThat(result).isZero();
    }

    @Test
    void 시작일이_null이면_데이터의_최소_날짜로_기간을_계산한다() {
        LocalDate startDate = null;
        LocalDate endDate = LocalDate.now();
        LocalDate minDate = endDate.minusDays(DATE_RANGE_OVERLAP_DAYS);
        LocalDate maxDate = endDate;

        List<DailyTrendStatisticsDto.DailyPrCountDto> createdCounts = List.of(
                new DailyTrendStatisticsDto.DailyPrCountDto(minDate, COUNT_ONE)
        );
        List<DailyTrendStatisticsDto.DailyPrCountDto> mergedCounts = List.of(
                new DailyTrendStatisticsDto.DailyPrCountDto(maxDate, COUNT_ONE)
        );

        long result = ReflectionTestUtils.invokeMethod(
                dailyTrendStatisticsQueryService,
                "calculateDateRangeDays",
                startDate,
                endDate,
                createdCounts,
                mergedCounts
        );

        assertThat(result).isEqualTo(DATE_RANGE_TWO_DAYS);
    }

    @Test
    void 종료일이_null이면_데이터의_최대_날짜로_기간을_계산한다() {
        LocalDate startDate = LocalDate.now().minusDays(DATE_RANGE_OVERLAP_DAYS);
        LocalDate endDate = null;
        LocalDate minDate = startDate;
        LocalDate maxDate = startDate.plusDays(DATE_RANGE_OVERLAP_DAYS);

        List<DailyTrendStatisticsDto.DailyPrCountDto> createdCounts = List.of(
                new DailyTrendStatisticsDto.DailyPrCountDto(minDate, COUNT_ONE)
        );
        List<DailyTrendStatisticsDto.DailyPrCountDto> mergedCounts = List.of(
                new DailyTrendStatisticsDto.DailyPrCountDto(maxDate, COUNT_ONE)
        );

        long result = ReflectionTestUtils.invokeMethod(
                dailyTrendStatisticsQueryService,
                "calculateDateRangeDays",
                startDate,
                endDate,
                createdCounts,
                mergedCounts
        );

        assertThat(result).isEqualTo(DATE_RANGE_TWO_DAYS);
    }

    @Test
    void 시작일과_종료일이_모두_null이면_데이터_범위로_기간을_계산한다() {
        LocalDate minDate = LocalDate.now().minusDays(DATE_RANGE_DAYS);
        LocalDate maxDate = minDate.plusDays(DATE_RANGE_OVERLAP_DAYS);

        List<DailyTrendStatisticsDto.DailyPrCountDto> createdCounts = List.of(
                new DailyTrendStatisticsDto.DailyPrCountDto(minDate, COUNT_ONE)
        );
        List<DailyTrendStatisticsDto.DailyPrCountDto> mergedCounts = List.of(
                new DailyTrendStatisticsDto.DailyPrCountDto(maxDate, COUNT_ONE)
        );

        long result = ReflectionTestUtils.invokeMethod(
                dailyTrendStatisticsQueryService,
                "calculateDateRangeDays",
                null,
                null,
                createdCounts,
                mergedCounts
        );

        assertThat(result).isEqualTo(DATE_RANGE_TWO_DAYS);
    }

    @Test
    void 데이터가_없고_기간이_null이면_0일을_반환한다() {
        long result = ReflectionTestUtils.invokeMethod(
                dailyTrendStatisticsQueryService,
                "calculateDateRangeDays",
                null,
                null,
                List.of(),
                List.of()
        );

        assertThat(result).isEqualTo(ZERO_COUNT);
    }

    @Test
    void 종료일이_시작일보다_이르면_0일을_반환한다() {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.minusDays(DATE_RANGE_OVERLAP_DAYS);

        long result = ReflectionTestUtils.invokeMethod(
                dailyTrendStatisticsQueryService,
                "calculateDateRangeDays",
                startDate,
                endDate,
                List.of(),
                List.of()
        );

        assertThat(result).isEqualTo(ZERO_COUNT);
    }

    @Test
    void 최소_최대_날짜_계산시_두_리스트를_합산한다() {
        LocalDate minDate = LocalDate.now().minusDays(DATE_RANGE_DAYS);
        LocalDate maxDate = minDate.plusDays(DATE_RANGE_OVERLAP_DAYS);

        List<DailyTrendStatisticsDto.DailyPrCountDto> createdCounts = List.of(
                new DailyTrendStatisticsDto.DailyPrCountDto(maxDate, COUNT_TWO)
        );
        List<DailyTrendStatisticsDto.DailyPrCountDto> mergedCounts = List.of(
                new DailyTrendStatisticsDto.DailyPrCountDto(minDate, COUNT_THREE)
        );

        LocalDate resolvedMin = ReflectionTestUtils.invokeMethod(
                dailyTrendStatisticsQueryService,
                "resolveMinDate",
                createdCounts,
                mergedCounts
        );
        LocalDate resolvedMax = ReflectionTestUtils.invokeMethod(
                dailyTrendStatisticsQueryService,
                "resolveMaxDate",
                createdCounts,
                mergedCounts
        );

        assertAll(
                () -> assertThat(resolvedMin).isEqualTo(minDate),
                () -> assertThat(resolvedMax).isEqualTo(maxDate)
        );
    }


    private Project createAndSaveProject(Long userId) {
        Project project = Project.create(TEST_PROJECT_NAME, TEST_API_KEY_PREFIX + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private void createAndSavePullRequest(Long projectId, LocalDateTime createdAt, LocalDateTime mergedAt) {
        PullRequestTiming timing = createTiming(createdAt, mergedAt);
        PullRequestState state = resolveState(mergedAt);

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

        pullRequestRepository.save(pullRequest);
    }

    private PullRequestTiming createTiming(LocalDateTime createdAt, LocalDateTime mergedAt) {
        if (mergedAt == null) {
            return PullRequestTiming.createOpen(createdAt);
        }
        return PullRequestTiming.createMerged(createdAt, mergedAt);
    }

    private PullRequestState resolveState(LocalDateTime mergedAt) {
        if (mergedAt == null) {
            return PullRequestState.OPEN;
        }
        return PullRequestState.MERGED;
    }
}
