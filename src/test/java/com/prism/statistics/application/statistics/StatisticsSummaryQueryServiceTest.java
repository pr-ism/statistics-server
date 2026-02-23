package com.prism.statistics.application.statistics;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.StatisticsSummaryRequest;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse;
import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.insight.lifecycle.PullRequestLifecycle;
import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.infrastructure.analysis.insight.persistence.*;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertAll;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StatisticsSummaryQueryServiceTest {

    @Autowired
    private StatisticsSummaryQueryService statisticsSummaryQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaPullRequestSizeRepository sizeRepository;

    @Autowired
    private JpaReviewActivityRepository reviewActivityRepository;

    @Autowired
    private JpaReviewSessionRepository reviewSessionRepository;

    @Autowired
    private JpaPullRequestLifecycleRepository lifecycleRepository;

    @Autowired
    private JpaPullRequestBottleneckRepository bottleneckRepository;

    @Test
    void 프로젝트의_통계_요약을_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        PullRequest pr1 = createAndSavePullRequest(project.getId(), PullRequestState.MERGED);
        PullRequest pr2 = createAndSavePullRequest(project.getId(), PullRequestState.CLOSED);

        createAndSaveSize(pr1.getId(), BigDecimal.valueOf(150));
        createAndSaveSize(pr2.getId(), BigDecimal.valueOf(50));

        createAndSaveReviewActivity(pr1.getId(), 2, 5);
        createAndSaveReviewActivity(pr2.getId(), 1, 2);

        createAndSaveReviewSession(pr1.getId(), 101L, "reviewer1");
        createAndSaveReviewSession(pr1.getId(), 102L, "reviewer2");
        createAndSaveReviewSession(pr2.getId(), 101L, "reviewer1");

        createAndSaveLifecycle(pr1.getId(), false);
        createAndSaveLifecycle(pr2.getId(), true);

        createAndSaveBottleneck(pr1.getId(), 60L, 120L, 30L);
        createAndSaveBottleneck(pr2.getId(), 90L, 60L, null);

        StatisticsSummaryRequest request = new StatisticsSummaryRequest(null, null);

        // when
        StatisticsSummaryResponse response = statisticsSummaryQueryService
                .findStatisticsSummary(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.overview().totalPrCount()).isEqualTo(2),
                () -> assertThat(response.overview().mergedPrCount()).isEqualTo(1),
                () -> assertThat(response.overview().closedPrCount()).isEqualTo(1),
                () -> assertThat(response.overview().mergeSuccessRate()).isEqualTo(50.0),
                () -> assertThat(response.overview().avgSizeScore()).isCloseTo(100.0, within(0.01)),
                () -> assertThat(response.reviewHealth().reviewRate()).isGreaterThan(0),
                () -> assertThat(response.teamActivity().totalReviewerCount()).isEqualTo(2),
                () -> assertThat(response.bottleneck().avgReviewWaitMinutes()).isCloseTo(75.0, within(0.01))
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        StatisticsSummaryRequest request = new StatisticsSummaryRequest(null, null);

        // when
        StatisticsSummaryResponse response = statisticsSummaryQueryService
                .findStatisticsSummary(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.overview().totalPrCount()).isZero(),
                () -> assertThat(response.overview().mergedPrCount()).isZero(),
                () -> assertThat(response.overview().mergeSuccessRate()).isZero(),
                () -> assertThat(response.reviewHealth().reviewRate()).isZero(),
                () -> assertThat(response.teamActivity().totalReviewerCount()).isZero(),
                () -> assertThat(response.bottleneck().avgReviewWaitMinutes()).isZero()
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId(), PullRequestState.MERGED);
        createAndSaveSize(pr.getId(), BigDecimal.valueOf(100));

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);
        StatisticsSummaryRequest request = new StatisticsSummaryRequest(startDate, endDate);

        // when
        StatisticsSummaryResponse response = statisticsSummaryQueryService
                .findStatisticsSummary(userId, project.getId(), request);

        // then
        assertThat(response.overview().totalPrCount()).isEqualTo(1);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long otherUserId = 999L;
        Project project = createAndSaveProject(userId);
        StatisticsSummaryRequest request = new StatisticsSummaryRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                statisticsSummaryQueryService.findStatisticsSummary(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 머지된_PR이_없으면_Merge_관련_통계는_0이다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId(), PullRequestState.CLOSED);
        createAndSaveSize(pr.getId(), BigDecimal.valueOf(100));

        StatisticsSummaryRequest request = new StatisticsSummaryRequest(null, null);

        // when
        StatisticsSummaryResponse response = statisticsSummaryQueryService
                .findStatisticsSummary(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.overview().mergedPrCount()).isZero(),
                () -> assertThat(response.overview().closedPrCount()).isEqualTo(1),
                () -> assertThat(response.overview().mergeSuccessRate()).isZero(),
                () -> assertThat(response.overview().avgMergeTimeMinutes()).isZero(),
                () -> assertThat(response.overview().avgSizeScore()).isCloseTo(100.0, within(0.01))
        );
    }

    @Test
    void 리뷰_세션이_없으면_팀_활동_통계는_0이다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId(), PullRequestState.MERGED);

        StatisticsSummaryRequest request = new StatisticsSummaryRequest(null, null);

        // when
        StatisticsSummaryResponse response = statisticsSummaryQueryService
                .findStatisticsSummary(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.teamActivity().totalReviewerCount()).isZero(),
                () -> assertThat(response.teamActivity().avgReviewersPerPr()).isZero(),
                () -> assertThat(response.teamActivity().avgReviewRoundTrips()).isZero(),
                () -> assertThat(response.teamActivity().avgCommentCount()).isZero()
        );
    }

    @Test
    void 병목_데이터가_없으면_병목_통계는_0이다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId(), PullRequestState.MERGED);

        StatisticsSummaryRequest request = new StatisticsSummaryRequest(null, null);

        // when
        StatisticsSummaryResponse response = statisticsSummaryQueryService
                .findStatisticsSummary(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.bottleneck().avgReviewWaitMinutes()).isZero(),
                () -> assertThat(response.bottleneck().avgReviewProgressMinutes()).isZero(),
                () -> assertThat(response.bottleneck().avgMergeWaitMinutes()).isZero(),
                () -> assertThat(response.bottleneck().totalCycleTimeMinutes()).isZero()
        );
    }

    @Test
    void 리뷰_없이_종료된_PR_비율을_계산한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr1 = createAndSavePullRequest(project.getId(), PullRequestState.MERGED);
        PullRequest pr2 = createAndSavePullRequest(project.getId(), PullRequestState.CLOSED);

        createAndSaveLifecycle(pr1.getId(), false);
        createAndSaveLifecycle(pr2.getId(), true);

        StatisticsSummaryRequest request = new StatisticsSummaryRequest(null, null);

        // when
        StatisticsSummaryResponse response = statisticsSummaryQueryService
                .findStatisticsSummary(userId, project.getId(), request);

        // then
        assertThat(response.reviewHealth().closedWithoutReviewRate()).isCloseTo(50.0, within(0.01));
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create("Test Project", "test-api-key-" + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId, PullRequestState state) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime closedAt = state.isClosureState() ? LocalDateTime.now() : null;

        PullRequestTiming timing = state.isMerged()
                ? PullRequestTiming.createMerged(createdAt, closedAt)
                : state.isClosed()
                ? PullRequestTiming.createClosed(createdAt, closedAt)
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

    private void createAndSaveSize(Long pullRequestId, BigDecimal sizeScore) {
        PullRequestSize size = PullRequestSize.create(
                pullRequestId,
                100,
                50,
                5,
                sizeScore
        );
        sizeRepository.save(size);
    }

    private void createAndSaveReviewActivity(Long pullRequestId, int roundTrips, int commentCount) {
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(pullRequestId)
                .reviewRoundTrips(roundTrips)
                .totalCommentCount(commentCount)
                .codeAdditionsAfterReview(0)
                .codeDeletionsAfterReview(0)
                .additionalReviewerCount(0)
                .totalAdditions(100)
                .totalDeletions(50)
                .build();
        reviewActivityRepository.save(activity);
    }

    private void createAndSaveReviewSession(Long pullRequestId, Long reviewerId, String reviewerName) {
        ReviewSession session = ReviewSession.create(
                pullRequestId,
                GithubUser.create(reviewerName, reviewerId),
                LocalDateTime.now().minusHours(1)
        );
        reviewSessionRepository.save(session);
    }

    private void createAndSaveLifecycle(Long pullRequestId, boolean closedWithoutReview) {
        PullRequestLifecycle lifecycle = PullRequestLifecycle.builder()
                .pullRequestId(pullRequestId)
                .reviewReadyAt(LocalDateTime.now().minusDays(1))
                .timeToMerge(closedWithoutReview ? null : DurationMinutes.of(1440L))
                .totalLifespan(DurationMinutes.of(1440L))
                .activeWork(DurationMinutes.of(120L))
                .stateChangeCount(1)
                .reopened(false)
                .closedWithoutReview(closedWithoutReview)
                .build();
        lifecycleRepository.save(lifecycle);
    }

    private void createAndSaveBottleneck(Long pullRequestId, Long reviewWait, Long reviewProgress, Long mergeWait) {
        LocalDateTime now = LocalDateTime.now();
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                pullRequestId,
                now.minusMinutes(reviewWait),
                now
        );

        if (reviewProgress != null) {
            bottleneck.updateOnNewReview(now.plusMinutes(reviewProgress), mergeWait != null);
        }

        if (reviewProgress != null && mergeWait != null) {
            bottleneck.updateOnMerge(now.plusMinutes(reviewProgress + mergeWait));
        }

        bottleneckRepository.save(bottleneck);
    }
}
