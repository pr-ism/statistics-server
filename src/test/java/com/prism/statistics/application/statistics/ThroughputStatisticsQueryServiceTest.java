package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertAll;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ThroughputStatisticsQueryServiceTest {

    @Autowired
    private ThroughputStatisticsQueryService throughputStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Test
    void 프로젝트의_처리량_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, 1440L);
        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, 2880L);
        createAndSavePullRequest(project.getId(), PullRequestState.CLOSED, null);

        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(null, null);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.mergedPrCount()).isEqualTo(2),
                () -> assertThat(response.closedPrCount()).isEqualTo(1),
                () -> assertThat(response.avgMergeTimeMinutes()).isCloseTo(2160.0, within(1.0)),
                () -> assertThat(response.mergeSuccessRate()).isCloseTo(66.67, within(0.01)),
                () -> assertThat(response.closedPrRate()).isCloseTo(33.33, within(0.01))
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = 1L;
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
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, 1440L);

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);
        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(startDate, endDate);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertThat(response.mergedPrCount()).isEqualTo(1);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long otherUserId = 999L;
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
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        createAndSavePullRequest(project.getId(), PullRequestState.CLOSED, null);

        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(null, null);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.mergedPrCount()).isZero(),
                () -> assertThat(response.closedPrCount()).isEqualTo(1),
                () -> assertThat(response.avgMergeTimeMinutes()).isZero(),
                () -> assertThat(response.mergeSuccessRate()).isZero(),
                () -> assertThat(response.closedPrRate()).isEqualTo(100.0)
        );
    }

    @Test
    void 모든_PR이_머지되면_머지_성공률은_100이다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, 1440L);
        createAndSavePullRequest(project.getId(), PullRequestState.MERGED, 2880L);

        ThroughputStatisticsRequest request = new ThroughputStatisticsRequest(null, null);

        // when
        ThroughputStatisticsResponse response = throughputStatisticsQueryService
                .findThroughputStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.mergedPrCount()).isEqualTo(2),
                () -> assertThat(response.closedPrCount()).isZero(),
                () -> assertThat(response.mergeSuccessRate()).isEqualTo(100.0),
                () -> assertThat(response.closedPrRate()).isZero()
        );
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create("Test Project", "test-api-key-" + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId, PullRequestState state, Long mergeTimeMinutes) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime closedAt = LocalDateTime.now();

        PullRequestTiming timing = state.isMerged()
                ? PullRequestTiming.createMerged(closedAt.minusMinutes(mergeTimeMinutes), closedAt)
                : PullRequestTiming.createClosed(createdAt, closedAt);

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
}
