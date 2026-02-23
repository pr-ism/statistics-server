package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WeeklyTrendStatisticsQueryServiceTest {

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
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        LocalDateTime now = LocalDateTime.now();
        PullRequest pr1 = createAndSavePullRequest(project.getId(), now.minusDays(8), now.minusDays(1)); // 지난주 생성 및 머지
        PullRequest pr2 = createAndSavePullRequest(project.getId(), now.minusDays(2), null); // 이번주 생성

        createAndSavePullRequestSize(pr1.getId(), 75, 5);
        createAndSavePullRequestSize(pr2.getId(), 90, 5);
        createAndSaveBottleneck(pr1.getId(), 120L); // 리뷰 대기 시간 데이터 추가

        WeeklyTrendStatisticsRequest request = new WeeklyTrendStatisticsRequest(
                LocalDate.now().minusWeeks(2),
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
        Long userId = 1L;
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
        Long userId = 1L;
        Long otherUserId = 999L;
        Project project = createAndSaveProject(userId);
        WeeklyTrendStatisticsRequest request = new WeeklyTrendStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }


    private Project createAndSaveProject(Long userId) {
        Project project = Project.create("Test Project", "test-api-key-" + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId, LocalDateTime createdAt, LocalDateTime mergedAt) {
        PullRequestState state = (mergedAt != null) ? PullRequestState.MERGED : PullRequestState.OPEN;
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
                .state(state)
                .link("https://github.com/test/repo/pull/1")
                .changeStats(PullRequestChangeStats.create(2, 10, 6))
                .commitCount(4)
                .timing(timing)
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private void createAndSavePullRequestSize(Long pullRequestId, int totalChanges, int fileCount) {
        int additions = totalChanges / 2;
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

    private void createAndSaveBottleneck(Long pullRequestId, Long reviewWaitMinutes) {
        LocalDateTime now = LocalDateTime.now();
        com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck bottleneck = com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck.createOnFirstReview(
                pullRequestId,
                now.minusMinutes(reviewWaitMinutes),
                now
        );
        bottleneckRepository.save(bottleneck);
    }
}
