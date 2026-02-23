package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.CollaborationStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.ReviewerStats;
import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck;
import com.prism.statistics.domain.analysis.insight.review.ReviewSession;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto.ReviewerResponseTimeDto;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestBottleneckRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewSessionRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollaborationStatisticsQueryServiceTest {

    @Autowired
    private CollaborationStatisticsQueryService collaborationStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaReviewRepository reviewRepository;

    @Autowired
    private JpaReviewSessionRepository reviewSessionRepository;

    @Autowired
    private JpaPullRequestBottleneckRepository bottleneckRepository;

    @Test
    void 프로젝트의_협업_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReview(pr1, 101L, "reviewer1");
        createAndSaveReview(pr1, 102L, "reviewer2");
        createAndSaveReview(pr2, 101L, "reviewer1");

        createAndSaveReviewSession(pr1.getId(), 101L, "reviewer1");
        createAndSaveReviewSession(pr1.getId(), 102L, "reviewer2");
        createAndSaveReviewSession(pr2.getId(), 101L, "reviewer1");

        createAndSaveBottleneck(pr1.getId(), 60L);
        createAndSaveBottleneck(pr2.getId(), 120L);

        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(null, null);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(2),
                () -> assertThat(response.reviewedPullRequestCount()).isGreaterThan(0),
                () -> assertThat(response.reviewerConcentration().totalReviewerCount()).isEqualTo(2),
                () -> assertThat(response.reviewerStats()).isNotEmpty()
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(null, null);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isZero(),
                () -> assertThat(response.reviewedPullRequestCount()).isZero(),
                () -> assertThat(response.reviewerConcentration().totalReviewerCount()).isZero(),
                () -> assertThat(response.reviewerStats()).isEmpty()
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr, 101L, "reviewer1");

        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now().plusDays(1);
        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(startDate, endDate);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(1);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = 1L;
        Long otherUserId = 999L;
        Project project = createAndSaveProject(userId);
        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                collaborationStatisticsQueryService.findCollaborationStatistics(otherUserId, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 리뷰어_집중도_지니_계수를_계산한다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());
        PullRequest pr3 = createAndSavePullRequest(project.getId());

        // reviewer1이 3번, reviewer2가 1번 리뷰
        createAndSaveReview(pr1, 101L, "reviewer1");
        createAndSaveReview(pr2, 101L, "reviewer1");
        createAndSaveReview(pr3, 101L, "reviewer1");
        createAndSaveReview(pr3, 102L, "reviewer2");

        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(null, null);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.reviewerConcentration().giniCoefficient()).isGreaterThan(0),
                () -> assertThat(response.reviewerConcentration().top3ReviewerRate()).isEqualTo(100.0)
        );
    }

    @Test
    void 리뷰어가_한_명일_때_지니_계수는_0이다() {
        // given
        Long userId = 1L;
        Project project = createAndSaveProject(userId);

        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr, 101L, "reviewer1");

        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(null, null);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertThat(response.reviewerConcentration().giniCoefficient()).isEqualTo(0.0);
    }

    @Test
    void reviewerResponseTime에_중복키가_있어도_첫번째_값이_유지된다() {
        Map<Long, Long> reviewerReviewCounts = Map.of(1L, 2L);
        List<ReviewerResponseTimeDto> responseTimes = List.of(
                new ReviewerResponseTimeDto(1L, "reviewer1", 100L, 2L),
                new ReviewerResponseTimeDto(1L, "reviewer1-dup", 200L, 4L)
        );

        @SuppressWarnings("unchecked")
        List<ReviewerStats> stats = (List<ReviewerStats>) ReflectionTestUtils.invokeMethod(
                collaborationStatisticsQueryService,
                "buildReviewerStats",
                reviewerReviewCounts,
                responseTimes
        );

        assertThat(stats).hasSize(1);
        assertThat(stats.getFirst().reviewerName()).isEqualTo("reviewer1");
        assertThat(stats.getFirst().avgResponseTimeMinutes()).isEqualTo(50.0);
    }

    @Test
    void 전체_건수가_0이면_퍼센트는_0이다() {
        double result = ReflectionTestUtils.invokeMethod(
                collaborationStatisticsQueryService,
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

    private void createAndSaveReview(PullRequest pullRequest, Long reviewerId, String reviewerName) {
        Review review = Review.builder()
                .githubPullRequestId(pullRequest.getGithubPullRequestId())
                .pullRequestNumber(pullRequest.getPullRequestNumber())
                .githubReviewId(System.nanoTime())
                .reviewer(GithubUser.create(reviewerName, reviewerId))
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(1)
                .githubSubmittedAt(LocalDateTime.now())
                .build();
        review.assignPullRequestId(pullRequest.getId());

        reviewRepository.save(review);
    }

    private void createAndSaveReviewSession(Long pullRequestId, Long reviewerId, String reviewerName) {
        ReviewSession session = ReviewSession.create(
                pullRequestId,
                GithubUser.create(reviewerName, reviewerId),
                LocalDateTime.now().minusHours(1)
        );
        reviewSessionRepository.save(session);
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
}
