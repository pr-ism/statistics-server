package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.PrSizeStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.PrSizeStatisticsResponse;
import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestBottleneckRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestSizeRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewActivityRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrSizeStatisticsQueryServiceTest {

    @Autowired
    private PrSizeStatisticsQueryService prSizeStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaPullRequestSizeRepository sizeRepository;

    @Autowired
    private JpaPullRequestBottleneckRepository bottleneckRepository;

    @Autowired
    private JpaReviewActivityRepository reviewActivityRepository;

    @Test
    void 프로젝트의_PR_크기_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveSize(pr1.getId(), 50, 20, 3);
        createAndSaveSize(pr2.getId(), 200, 100, 10);

        createAndSaveBottleneck(pr1.getId(), 60L);
        createAndSaveBottleneck(pr2.getId(), 120L);

        createAndSaveReviewActivity(pr1.getId(), 1);
        createAndSaveReviewActivity(pr2.getId(), 3);

        PrSizeStatisticsRequest request = new PrSizeStatisticsRequest(null, null);

        // when
        PrSizeStatisticsResponse response = prSizeStatisticsQueryService
                .findPrSizeStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPrCount()).isEqualTo(2),
                () -> assertThat(response.avgSizeScore()).isGreaterThan(0),
                () -> assertThat(response.sizeGradeDistribution()).isNotEmpty()
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PrSizeStatisticsRequest request = new PrSizeStatisticsRequest(null, null);

        // when
        PrSizeStatisticsResponse response = prSizeStatisticsQueryService
                .findPrSizeStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPrCount()).isZero(),
                () -> assertThat(response.avgSizeScore()).isZero(),
                () -> assertThat(response.largePrRate()).isZero()
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr.getId(), 50, 20, 3);

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);
        PrSizeStatisticsRequest request = new PrSizeStatisticsRequest(startDate, endDate);

        // when
        PrSizeStatisticsResponse response = prSizeStatisticsQueryService
                .findPrSizeStatistics(userId, project.getId(), request);

        // then
        assertThat(response.totalPrCount()).isEqualTo(1);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long otherUserId = 999L;
        Project project = createAndSaveProject(userId);
        PrSizeStatisticsRequest request = new PrSizeStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                prSizeStatisticsQueryService.findPrSizeStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 모든_크기_등급이_분포에_포함된다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr.getId(), 50, 20, 3);

        PrSizeStatisticsRequest request = new PrSizeStatisticsRequest(null, null);

        // when
        PrSizeStatisticsResponse response = prSizeStatisticsQueryService
                .findPrSizeStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.sizeGradeDistribution()).containsKey(SizeGrade.S),
                () -> assertThat(response.sizeGradeDistribution()).containsKey(SizeGrade.M),
                () -> assertThat(response.sizeGradeDistribution()).containsKey(SizeGrade.L),
                () -> assertThat(response.sizeGradeDistribution()).containsKey(SizeGrade.XL)
        );
    }

    @Test
    void 대형_PR_비율을_계산한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        // 작은 PR
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr1.getId(), 10, 5, 1);

        // 큰 PR (L 이상)
        PullRequest pr2 = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr2.getId(), 500, 200, 20);

        PrSizeStatisticsRequest request = new PrSizeStatisticsRequest(null, null);

        // when
        PrSizeStatisticsResponse response = prSizeStatisticsQueryService
                .findPrSizeStatistics(userId, project.getId(), request);

        // then
        assertThat(response.largePrRate()).isCloseTo(50.0, within(0.01));
    }

    @Test
    void 상관관계_데이터가_충분하면_상관계수를_계산한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        for (int i = 0; i < 5; i++) {
            PullRequest pr = createAndSavePullRequest(project.getId());
            createAndSaveSize(pr.getId(), (i + 1) * 50, (i + 1) * 20, (i + 1) * 2);
            createAndSaveBottleneck(pr.getId(), (i + 1) * 30L);
            createAndSaveReviewActivity(pr.getId(), i + 1);
        }

        PrSizeStatisticsRequest request = new PrSizeStatisticsRequest(null, null);

        // when
        PrSizeStatisticsResponse response = prSizeStatisticsQueryService
                .findPrSizeStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.sizeReviewWaitCorrelation().interpretation()).isNotEqualTo("데이터 부족"),
                () -> assertThat(response.sizeReviewRoundTripCorrelation().interpretation()).isNotEqualTo("데이터 부족")
        );
    }

    @Test
    void 상관관계_데이터가_부족하면_데이터_부족으로_표시된다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr.getId(), 50, 20, 3);

        PrSizeStatisticsRequest request = new PrSizeStatisticsRequest(null, null);

        // when
        PrSizeStatisticsResponse response = prSizeStatisticsQueryService
                .findPrSizeStatistics(userId, project.getId(), request);

        // then
        assertThat(response.sizeReviewWaitCorrelation().interpretation()).isEqualTo("데이터 부족");
    }

    @Test
    void 전체_건수가_0이면_퍼센트는_0이다() {
        double result = ReflectionTestUtils.invokeMethod(
                prSizeStatisticsQueryService,
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

    private void createAndSaveSize(Long pullRequestId, int additions, int deletions, int changedFiles) {
        PullRequestSize size = PullRequestSize.create(
                pullRequestId,
                additions,
                deletions,
                changedFiles,
                BigDecimal.valueOf(0.3)
        );
        sizeRepository.save(size);
    }

    private void createAndSaveBottleneck(Long pullRequestId, Long reviewWaitMinutes) {
        LocalDateTime now = LocalDateTime.now();
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                pullRequestId,
                now.minusMinutes(reviewWaitMinutes),
                now
        );
        bottleneckRepository.save(bottleneck);
    }

    private void createAndSaveReviewActivity(Long pullRequestId, int roundTrips) {
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(pullRequestId)
                .reviewRoundTrips(roundTrips)
                .totalCommentCount(roundTrips * 2)
                .codeAdditionsAfterReview(0)
                .codeDeletionsAfterReview(0)
                .additionalReviewerCount(0)
                .totalAdditions(100)
                .totalDeletions(50)
                .build();
        reviewActivityRepository.save(activity);
    }
}
