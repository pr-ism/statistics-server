package com.prism.statistics.application.statistics;

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
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto.AuthorReviewWaitTimeDto;
import com.prism.statistics.domain.statistics.repository.dto.CollaborationStatisticsDto.ReviewerResponseTimeDto;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestBottleneckRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewSessionRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewRepository;
import com.prism.statistics.infrastructure.project.persistence.JpaProjectRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollaborationStatisticsQueryServiceTest {

    private static final long DEFAULT_USER_ID = 1L;
    private static final long OTHER_USER_ID = 999L;
    private static final long REVIEWER_ID_1 = 101L;
    private static final long REVIEWER_ID_2 = 102L;
    private static final long AUTHOR_USER_ID = 1L;
    private static final long SINGLE_REVIEWER_ID = 1L;
    private static final String REVIEWER_NAME_1 = "reviewer1";
    private static final String REVIEWER_NAME_2 = "reviewer2";
    private static final String AUTHOR_NAME_1 = "author-1";
    private static final String AUTHOR_NAME_2 = "author-2";
    private static final String DUP_REVIEWER_NAME = "reviewer1-dup";
    private static final long REVIEWER_COUNT = 2L;
    private static final long PULL_REQUEST_COUNT_ONE = 1L;
    private static final long PULL_REQUEST_COUNT_TWO = 2L;
    private static final long REVIEW_COUNT_TWO = 2L;
    private static final long REVIEW_COUNT_FOUR = 4L;
    private static final long TOTAL_REVIEW_TIME_100 = 100L;
    private static final long TOTAL_REVIEW_TIME_200 = 200L;
    private static final double EXPECTED_REVIEWER_RATE = 100.0;
    private static final double EXPECTED_AVG_RESPONSE_TIME = 50.0;
    private static final long REVIEW_WAIT_MINUTES_60 = 60L;
    private static final long REVIEW_WAIT_MINUTES_120 = 120L;
    private static final long REVIEW_WAIT_MINUTES_30 = 30L;
    private static final long DATE_RANGE_DAYS = 7L;
    private static final long DATE_RANGE_EXTRA_DAYS = 1L;
    private static final long COUNT_ONE = 1L;
    private static final long COUNT_ZERO = 0L;
    private static final double ZERO_DOUBLE = 0.0;
    private static final String TEST_PROJECT_NAME = "Test Project";
    private static final String TEST_API_KEY_PREFIX = "test-api-key-";
    private static final String TEST_USER_NAME = "testuser";
    private static final String TEST_PULL_REQUEST_TITLE = "Test PR";
    private static final String TEST_PULL_REQUEST_URL = "https://github.com/test/repo/pull/1";
    private static final String TEST_HEAD_SHA = "abc123";
    private static final String REVIEW_BODY = "LGTM";
    private static final int CHANGE_STATS_ADDITIONS = 2;
    private static final int CHANGE_STATS_DELETIONS = 10;
    private static final int CHANGE_STATS_FILES = 6;
    private static final int COMMIT_COUNT = 4;
    private static final int PULL_REQUEST_NUMBER_MODULUS = 10000;
    private static final int REVIEW_COMMENT_COUNT = 1;
    private static final int REVIEW_SESSION_HOURS_AGO = 1;
    private static final int PULL_REQUEST_CREATED_DAYS_AGO = 1;
    private static final int LIST_SIZE_ONE = 1;
    private static final int LIST_SIZE_TWO = 2;
    private static final int SECOND_INDEX = 1;

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
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);

        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReview(pr1, REVIEWER_ID_1, REVIEWER_NAME_1);
        createAndSaveReview(pr1, REVIEWER_ID_2, REVIEWER_NAME_2);
        createAndSaveReview(pr2, REVIEWER_ID_1, REVIEWER_NAME_1);

        createAndSaveReviewSession(pr1.getId(), REVIEWER_ID_1, REVIEWER_NAME_1);
        createAndSaveReviewSession(pr1.getId(), REVIEWER_ID_2, REVIEWER_NAME_2);
        createAndSaveReviewSession(pr2.getId(), REVIEWER_ID_1, REVIEWER_NAME_1);

        createAndSaveBottleneck(pr1.getId(), REVIEW_WAIT_MINUTES_60);
        createAndSaveBottleneck(pr2.getId(), REVIEW_WAIT_MINUTES_120);

        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(null, null);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(PULL_REQUEST_COUNT_TWO),
                () -> assertThat(response.reviewedPullRequestCount()).isGreaterThan(COUNT_ZERO),
                () -> assertThat(response.reviewerConcentration().totalReviewerCount()).isEqualTo(REVIEWER_COUNT),
                () -> assertThat(response.reviewerStats()).isNotEmpty()
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Long userId = DEFAULT_USER_ID;
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
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr, REVIEWER_ID_1, REVIEWER_NAME_1);

        LocalDate startDate = LocalDate.now().minusDays(DATE_RANGE_DAYS);
        LocalDate endDate = LocalDate.now().plusDays(DATE_RANGE_EXTRA_DAYS);
        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(startDate, endDate);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(PULL_REQUEST_COUNT_ONE);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Long otherUserId = OTHER_USER_ID;
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
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);

        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());
        PullRequest pr3 = createAndSavePullRequest(project.getId());

        // reviewer1이 3번, reviewer2가 1번 리뷰
        createAndSaveReview(pr1, REVIEWER_ID_1, REVIEWER_NAME_1);
        createAndSaveReview(pr2, REVIEWER_ID_1, REVIEWER_NAME_1);
        createAndSaveReview(pr3, REVIEWER_ID_1, REVIEWER_NAME_1);
        createAndSaveReview(pr3, REVIEWER_ID_2, REVIEWER_NAME_2);

        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(null, null);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.reviewerConcentration().giniCoefficient()).isGreaterThan(ZERO_DOUBLE),
                () -> assertThat(response.reviewerConcentration().top3ReviewerRate()).isEqualTo(EXPECTED_REVIEWER_RATE)
        );
    }

    @Test
    void 리뷰어가_한_명일_때_지니_계수는_0이다() {
        // given
        Long userId = DEFAULT_USER_ID;
        Project project = createAndSaveProject(userId);

        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReview(pr, REVIEWER_ID_1, REVIEWER_NAME_1);

        CollaborationStatisticsRequest request = new CollaborationStatisticsRequest(null, null);

        // when
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService
                .findCollaborationStatistics(userId, project.getId(), request);

        // then
        assertThat(response.reviewerConcentration().giniCoefficient()).isEqualTo(ZERO_DOUBLE);
    }

    @Test
    void reviewerResponseTime에_중복키가_있어도_첫번째_값이_유지된다() {
        Map<Long, Long> reviewerReviewCounts = Map.of(SINGLE_REVIEWER_ID, REVIEW_COUNT_TWO);
        List<ReviewerResponseTimeDto> responseTimes = List.of(
                new ReviewerResponseTimeDto(SINGLE_REVIEWER_ID, REVIEWER_NAME_1, TOTAL_REVIEW_TIME_100, REVIEW_COUNT_TWO),
                new ReviewerResponseTimeDto(SINGLE_REVIEWER_ID, DUP_REVIEWER_NAME, TOTAL_REVIEW_TIME_200, REVIEW_COUNT_FOUR)
        );

        @SuppressWarnings("unchecked")
        List<ReviewerStats> stats = (List<ReviewerStats>) ReflectionTestUtils.invokeMethod(
                collaborationStatisticsQueryService,
                "buildReviewerStats",
                reviewerReviewCounts,
                responseTimes
        );

        assertThat(stats).hasSize(LIST_SIZE_ONE);
        assertThat(stats.getFirst().reviewerName()).isEqualTo(REVIEWER_NAME_1);
        assertThat(stats.getFirst().avgResponseTimeMinutes()).isEqualTo(EXPECTED_AVG_RESPONSE_TIME);
    }

    @Test
    void 작성자_리뷰_대기_시간은_평균_대기_시간_내림차순으로_정렬된다() {
        List<AuthorReviewWaitTimeDto> dtos = List.of(
                new AuthorReviewWaitTimeDto(1L, AUTHOR_NAME_1, REVIEW_WAIT_MINUTES_120, PULL_REQUEST_COUNT_TWO),
                new AuthorReviewWaitTimeDto(2L, AUTHOR_NAME_2, REVIEW_WAIT_MINUTES_30, PULL_REQUEST_COUNT_ONE)
        );

        @SuppressWarnings("unchecked")
        List<CollaborationStatisticsResponse.AuthorReviewWaitTime> result =
                (List<CollaborationStatisticsResponse.AuthorReviewWaitTime>)
                        ReflectionTestUtils.invokeMethod(
                                collaborationStatisticsQueryService,
                                "buildAuthorReviewWaitTimes",
                                dtos
                        );

        assertThat(result).hasSize(LIST_SIZE_TWO);
        assertThat(result.getFirst().authorName()).isEqualTo(AUTHOR_NAME_1);
        assertThat(result.getFirst().avgReviewWaitMinutes()).isGreaterThan(result.get(SECOND_INDEX).avgReviewWaitMinutes());
    }

    @Test
    void 전체_건수가_0이면_퍼센트는_0이다() {
        double result = ReflectionTestUtils.invokeMethod(
                collaborationStatisticsQueryService,
                "calculatePercentage",
                COUNT_ONE,
                COUNT_ZERO
        );

        assertThat(result).isZero();
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create(TEST_PROJECT_NAME, TEST_API_KEY_PREFIX + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(PULL_REQUEST_CREATED_DAYS_AGO);

        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create(TEST_USER_NAME, AUTHOR_USER_ID))
                .pullRequestNumber((int) (Math.abs(System.nanoTime() % PULL_REQUEST_NUMBER_MODULUS)))
                .headCommitSha(TEST_HEAD_SHA)
                .title(TEST_PULL_REQUEST_TITLE)
                .state(PullRequestState.OPEN)
                .link(TEST_PULL_REQUEST_URL)
                .changeStats(PullRequestChangeStats.create(CHANGE_STATS_ADDITIONS, CHANGE_STATS_DELETIONS, CHANGE_STATS_FILES))
                .commitCount(COMMIT_COUNT)
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
                .headCommitSha(TEST_HEAD_SHA)
                .body(REVIEW_BODY)
                .commentCount(REVIEW_COMMENT_COUNT)
                .githubSubmittedAt(LocalDateTime.now())
                .build();
        review.assignPullRequestId(pullRequest.getId());

        reviewRepository.save(review);
    }

    private void createAndSaveReviewSession(Long pullRequestId, Long reviewerId, String reviewerName) {
        ReviewSession session = ReviewSession.create(
                pullRequestId,
                GithubUser.create(reviewerName, reviewerId),
                LocalDateTime.now().minusHours(REVIEW_SESSION_HOURS_AGO)
        );
        reviewSessionRepository.save(session);
    }

    private void createAndSaveBottleneck(Long pullRequestId, Long reviewWaitMinutes) {
        LocalDateTime now = LocalDateTime.now();
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                pullRequestId,
                now.minusMinutes(reviewWaitMinutes),
                now,
                false
        );
        bottleneckRepository.save(bottleneck);
    }
}
