package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.LifecycleStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.LifecycleStatisticsResponse;
import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestLifecycleRepository;
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
class LifecycleStatisticsQueryServiceTest {

    @Autowired
    private LifecycleStatisticsQueryService lifecycleStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaPullRequestLifecycleRepository lifecycleRepository;

    @Test
    void 프로젝트의_PR_수명주기_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveLifecycle(pr1.getId(), true, false, 1710L, 1800L, 120L, 2, false);
        createAndSaveLifecycle(pr2.getId(), false, true, null, 1440L, 60L, 1, false);

        LifecycleStatisticsRequest request = new LifecycleStatisticsRequest(null, null);

        // when
        LifecycleStatisticsResponse response = lifecycleStatisticsQueryService
                .findLifecycleStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(2),
                () -> assertThat(response.mergedCount()).isEqualTo(1),
                () -> assertThat(response.closedWithoutMergeCount()).isEqualTo(1),
                () -> assertThat(response.mergeRate()).isEqualTo(50.0),
                () -> assertThat(response.averageTime().averageTimeToMergeMinutes()).isEqualTo(1710L),
                () -> assertThat(response.averageTime().averageLifespanMinutes()).isEqualTo(1620L),
                () -> assertThat(response.averageTime().averageActiveWorkMinutes()).isEqualTo(90L)
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveLifecycle(pr.getId(), true, false, 1000L, 1200L, 100L, 1, false);

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);
        LifecycleStatisticsRequest request = new LifecycleStatisticsRequest(startDate, endDate);

        // when
        LifecycleStatisticsResponse response = lifecycleStatisticsQueryService
                .findLifecycleStatistics(userId, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(1);
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        LifecycleStatisticsRequest request = new LifecycleStatisticsRequest(null, null);

        // when
        LifecycleStatisticsResponse response = lifecycleStatisticsQueryService
                .findLifecycleStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isZero(),
                () -> assertThat(response.mergedCount()).isZero(),
                () -> assertThat(response.mergeRate()).isZero(),
                () -> assertThat(response.averageTime().averageTimeToMergeMinutes()).isZero(),
                () -> assertThat(response.health().closedWithoutReviewCount()).isZero()
        );
    }

    @Test
    void 리뷰_없이_종료된_PR과_재오픈된_PR_건강_지표를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());
        PullRequest pr3 = createAndSavePullRequest(project.getId());

        createAndSaveLifecycle(pr1.getId(), true, false, 100L, 200L, 50L, 2, false);
        createAndSaveLifecycle(pr2.getId(), false, true, null, 300L, 60L, 3, true);
        createAndSaveLifecycle(pr3.getId(), true, false, 150L, 250L, 70L, 1, false);

        LifecycleStatisticsRequest request = new LifecycleStatisticsRequest(null, null);

        // when
        LifecycleStatisticsResponse response = lifecycleStatisticsQueryService
                .findLifecycleStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.health().closedWithoutReviewCount()).isEqualTo(1),
                () -> assertThat(response.health().closedWithoutReviewRate()).isEqualTo(33.33),
                () -> assertThat(response.health().reopenedCount()).isEqualTo(1),
                () -> assertThat(response.health().reopenedRate()).isEqualTo(33.33),
                () -> assertThat(response.health().averageStateChangeCount()).isEqualTo(2.0)
        );
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long otherUserId = 999L;
        Project project = createAndSaveProject(userId);
        LifecycleStatisticsRequest request = new LifecycleStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                lifecycleStatisticsQueryService.findLifecycleStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 머지된_PR이_없으면_평균_머지_시간은_0이다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveLifecycle(pr.getId(), false, true, null, 1440L, 60L, 1, false);

        LifecycleStatisticsRequest request = new LifecycleStatisticsRequest(null, null);

        // when
        LifecycleStatisticsResponse response = lifecycleStatisticsQueryService
                .findLifecycleStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.mergedCount()).isZero(),
                () -> assertThat(response.averageTime().averageTimeToMergeMinutes()).isZero()
        );
    }

    @Test
    void private_calculatePercentage_totalCount가_0이면_0을_반환한다() throws Exception {
        // given
        java.lang.reflect.Method method = LifecycleStatisticsQueryService.class
                .getDeclaredMethod("calculatePercentage", long.class, long.class);
        method.setAccessible(true);

        // when
        double result = (double) method.invoke(lifecycleStatisticsQueryService, 10L, 0L);

        // then
        assertThat(result).isEqualTo(0.0);
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
                .pullRequestNumber((int) (Math.abs(System.nanoTime() % 10000)))
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

    private void createAndSaveLifecycle(
            Long pullRequestId,
            boolean merged,
            boolean closedWithoutReview,
            Long timeToMergeMinutes,
            Long totalLifespanMinutes,
            Long activeWorkMinutes,
            int stateChangeCount,
            boolean reopened
    ) {
        PullRequestLifecycle lifecycle = PullRequestLifecycle.builder()
                .pullRequestId(pullRequestId)
                .reviewReadyAt(LocalDateTime.now().minusDays(1))
                .timeToMerge(timeToMergeMinutes != null ? DurationMinutes.of(timeToMergeMinutes) : null)
                .totalLifespan(totalLifespanMinutes != null ? DurationMinutes.of(totalLifespanMinutes) : null)
                .activeWork(DurationMinutes.of(activeWorkMinutes))
                .stateChangeCount(stateChangeCount)
                .reopened(reopened)
                .closedWithoutReview(closedWithoutReview)
                .build();

        lifecycleRepository.save(lifecycle);
    }
}
