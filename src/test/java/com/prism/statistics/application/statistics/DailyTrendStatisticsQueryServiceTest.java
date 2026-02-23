package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

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
class DailyTrendStatisticsQueryServiceTest {

    @Autowired
    private DailyTrendStatisticsQueryService dailyTrendStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Test
    void 일별_트렌드_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        LocalDateTime today = LocalDate.now().atStartOfDay();
        createAndSavePullRequest(project.getId(), today.minusDays(1), today); // 어제 생성, 오늘 머지
        createAndSavePullRequest(project.getId(), today.minusDays(2), null); // 그제 생성, 머지 안됨

        DailyTrendStatisticsRequest request = new DailyTrendStatisticsRequest(
                LocalDate.now().minusDays(3),
                LocalDate.now()
        );

        // when
        DailyTrendStatisticsResponse response = dailyTrendStatisticsQueryService
                .findDailyTrendStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.dailyCreatedTrend()).isNotEmpty(),
                () -> assertThat(response.dailyMergedTrend()).isNotEmpty(),
                () -> assertThat(response.summary().totalCreatedCount()).isEqualTo(2),
                () -> assertThat(response.summary().totalMergedCount()).isEqualTo(1),
                () -> assertThat(response.summary().avgDailyCreatedCount()).isGreaterThan(0),
                () -> assertThat(response.summary().avgDailyMergedCount()).isGreaterThan(0)
        );
    }

    @Test
    void 데이터가_없는_경우_빈_통계를_반환한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        DailyTrendStatisticsRequest request = new DailyTrendStatisticsRequest(
                LocalDate.now().minusDays(3),
                LocalDate.now()
        );

        // when
        DailyTrendStatisticsResponse response = dailyTrendStatisticsQueryService
                .findDailyTrendStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.dailyCreatedTrend()).isEmpty(),
                () -> assertThat(response.dailyMergedTrend()).isEmpty(),
                () -> assertThat(response.summary().totalCreatedCount()).isZero(),
                () -> assertThat(response.summary().totalMergedCount()).isZero()
        );
    }

    @Test
    void 소유하지_않은_프로젝트_조회시_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long otherUserId = 999L;
        Project project = createAndSaveProject(userId);
        DailyTrendStatisticsRequest request = new DailyTrendStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                dailyTrendStatisticsQueryService.findDailyTrendStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }


    private Project createAndSaveProject(Long userId) {
        Project project = Project.create("Test Project", "test-api-key-" + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private void createAndSavePullRequest(Long projectId, LocalDateTime createdAt, LocalDateTime mergedAt) {
        PullRequestTiming timing = (mergedAt != null)
                ? PullRequestTiming.createMerged(createdAt, mergedAt)
                : PullRequestTiming.createOpen(createdAt);

        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create("testuser", 1L))
                .pullRequestNumber((int) (Math.abs(System.nanoTime() % 10000)))
                .headCommitSha("abc123")
                .title("Test PR")
                .state(mergedAt != null ? PullRequestState.MERGED : PullRequestState.OPEN)
                .link("https://github.com/test/repo/pull/1")
                .changeStats(PullRequestChangeStats.create(2, 10, 6))
                .commitCount(4)
                .timing(timing)
                .build();

        pullRequestRepository.save(pullRequest);
    }
}
