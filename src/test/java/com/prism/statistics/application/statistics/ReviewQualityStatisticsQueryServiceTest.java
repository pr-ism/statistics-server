package com.prism.statistics.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.ReviewQualityStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse;
import com.prism.statistics.domain.analysis.insight.activity.ReviewActivity;
import com.prism.statistics.domain.analysis.insight.review.ReviewResponseTime;
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
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewActivityRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewResponseTimeRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaReviewSessionRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewRepository;
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
class ReviewQualityStatisticsQueryServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;
    private static final long FIRST_REVIEWER_ID = 1L;
    private static final long SECOND_REVIEWER_ID = 2L;
    private static final String FIRST_REVIEWER_NAME = "reviewer1";
    private static final String SECOND_REVIEWER_NAME = "reviewer2";
    private static final String TEST_USER_NAME = "testuser";
    private static final String TEST_PROJECT_NAME = "Test Project";
    private static final String TEST_API_KEY_PREFIX = "test-api-key-";
    private static final String TEST_HEAD_COMMIT_SHA = "abc123";
    private static final String TEST_PR_TITLE = "Test PR";
    private static final String TEST_PR_LINK = "https://github.com/test/repo/pull/1";
    private static final int PR_NUMBER_BOUND = 10000;
    private static final int DEFAULT_COMMIT_COUNT = 4;
    private static final int DEFAULT_CHANGE_STATS_ADDITIONS = 2;
    private static final int DEFAULT_CHANGE_STATS_DELETIONS = 10;
    private static final int DEFAULT_CHANGE_STATS_CHANGED_FILES = 6;
    private static final int DEFAULT_DAYS_RANGE = 7;
    private static final int ONE_DAY = 1;
    private static final int ZERO_INT = 0;
    private static final int ONE_INT = 1;
    private static final int TWO_INT = 2;
    private static final int THREE_INT = 3;
    private static final int FOUR_INT = 4;
    private static final int FIVE_INT = 5;
    private static final int EIGHT_INT = 8;
    private static final int TEN_INT = 10;
    private static final int TWELVE_INT = 12;
    private static final int FIFTEEN_INT = 15;
    private static final int TWENTY_INT = 20;
    private static final int THIRTY_INT = 30;
    private static final int FORTY_INT = 40;
    private static final int FORTY_FIVE_INT = 45;
    private static final int FIFTY_INT = 50;
    private static final int SIXTY_INT = 60;
    private static final int EIGHTY_INT = 80;
    private static final int HUNDRED_INT = 100;
    private static final long THIRTY_MINUTES = 30L;
    private static final long FORTY_FIVE_MINUTES = 45L;
    private static final long SIXTY_MINUTES = 60L;
    private static final long ONE_HOUR = 1L;
    private static final long TWO_HOURS = 2L;
    private static final long ONE_PR_COUNT = 1L;
    private static final long TWO_PR_COUNT = 2L;
    private static final double REVIEW_RATE_50 = 50.0;
    private static final double REVIEW_RATE_100 = 100.0;
    private static final double AVG_REVIEW_ROUND_TRIPS = 2.5;
    private static final double AVG_COMMENT_COUNT = 6.5;
    private static final double AVG_REVIEW_ROUND_TRIPS_SINGLE = 2.0;
    private static final double AVG_REVIEWERS_PER_PR = 1.0;
    private static final double AVG_SESSION_DURATION = 30.0;

    @Autowired
    private ReviewQualityStatisticsQueryService reviewQualityStatisticsQueryService;

    @Autowired
    private JpaProjectRepository projectRepository;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaReviewActivityRepository reviewActivityRepository;

    @Autowired
    private JpaReviewSessionRepository reviewSessionRepository;

    @Autowired
    private JpaReviewResponseTimeRepository reviewResponseTimeRepository;

    @Autowired
    private JpaReviewRepository reviewRepository;

    @Test
    void 프로젝트의_리뷰_품질_통계를_조회한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReviewActivity(pr1.getId(), TWO_INT, FIVE_INT, HUNDRED_INT, FIFTY_INT, true, FIFTEEN_INT);
        createAndSaveReviewActivity(pr2.getId(), THREE_INT, EIGHT_INT, EIGHTY_INT, FORTY_INT, false, FIVE_INT);

        createAndSaveReviewSession(pr1.getId(), FIRST_REVIEWER_ID, FIRST_REVIEWER_NAME, SIXTY_MINUTES, TWO_INT);
        createAndSaveReviewSession(pr1.getId(), SECOND_REVIEWER_ID, SECOND_REVIEWER_NAME, FORTY_FIVE_MINUTES, ONE_INT);
        createAndSaveReviewSession(pr2.getId(), FIRST_REVIEWER_ID, FIRST_REVIEWER_NAME, THIRTY_MINUTES, THREE_INT);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(TWO_PR_COUNT),
                () -> assertThat(response.reviewedPullRequestCount()).isEqualTo(TWO_PR_COUNT),
                () -> assertThat(response.reviewRate()).isEqualTo(REVIEW_RATE_100),
                () -> assertThat(response.reviewActivity().avgReviewRoundTrips()).isEqualTo(AVG_REVIEW_ROUND_TRIPS),
                () -> assertThat(response.reviewActivity().avgCommentCount()).isEqualTo(AVG_COMMENT_COUNT),
                () -> assertThat(response.reviewActivity().withAdditionalReviewersCount()).isEqualTo(ONE_INT),
                () -> assertThat(response.reviewActivity().withChangesAfterReviewCount()).isEqualTo(ONE_INT),
                () -> assertThat(response.reviewerStats().totalReviewerCount()).isEqualTo(TWO_PR_COUNT),
                () -> assertThat(response.reviewerStats().avgReviewersPerPr()).isEqualTo(AVG_REVIEWERS_PER_PR)
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReviewActivity(pr.getId(), ONE_INT, THREE_INT, FIFTY_INT, THIRTY_INT, false, ZERO_INT);

        LocalDate startDate = LocalDate.now().minusDays(DEFAULT_DAYS_RANGE);
        LocalDate endDate = LocalDate.now().plusDays(ONE_DAY);
        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(startDate, endDate);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(ONE_PR_COUNT);
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isZero(),
                () -> assertThat(response.reviewedPullRequestCount()).isZero(),
                () -> assertThat(response.reviewRate()).isZero(),
                () -> assertThat(response.reviewActivity().avgReviewRoundTrips()).isZero(),
                () -> assertThat(response.reviewerStats().totalReviewerCount()).isZero()
        );
    }

    @Test
    void 리뷰_활동만_있고_세션이_없는_경우_통계를_조회한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReviewActivity(pr.getId(), TWO_INT, FOUR_INT, SIXTY_INT, TWENTY_INT, true, TWELVE_INT);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(ONE_PR_COUNT),
                () -> assertThat(response.reviewActivity().avgReviewRoundTrips()).isEqualTo(AVG_REVIEW_ROUND_TRIPS_SINGLE),
                () -> assertThat(response.reviewerStats().totalReviewerCount()).isZero()
        );
    }

    @Test
    void 세션만_있고_리뷰_활동이_없는_경우_통계를_조회한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveReviewSession(pr.getId(), FIRST_REVIEWER_ID, FIRST_REVIEWER_NAME, THIRTY_MINUTES, ONE_INT);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isZero(),
                () -> assertThat(response.reviewerStats().totalReviewerCount()).isEqualTo(ONE_PR_COUNT),
                () -> assertThat(response.reviewerStats().avgSessionDurationMinutes()).isEqualTo(AVG_SESSION_DURATION)
        );
    }

    @Test
    void 리뷰되지_않은_PR도_포함하여_리뷰율을_계산한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReviewActivity(pr1.getId(), ONE_INT, THREE_INT, FIFTY_INT, THIRTY_INT, false, ZERO_INT);
        createAndSaveReviewActivityWithoutReview(pr2.getId(), FORTY_INT, TWENTY_INT);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(TWO_PR_COUNT),
                () -> assertThat(response.reviewedPullRequestCount()).isEqualTo(ONE_PR_COUNT),
                () -> assertThat(response.reviewRate()).isEqualTo(REVIEW_RATE_50)
        );
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                reviewQualityStatisticsQueryService.findReviewQualityStatistics(OTHER_USER_ID, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 변경_해결_건수가_있으면_평균_해결_시간을_계산한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr = createAndSavePullRequest(project.getId());

        createAndSaveReviewActivity(pr.getId(), TWO_INT, FOUR_INT, SIXTY_INT, TWENTY_INT, true, TWELVE_INT);

        LocalDateTime changesRequestedAt = LocalDateTime.now().minusHours(TWO_HOURS);
        LocalDateTime approvedAt = LocalDateTime.now().minusHours(ONE_HOUR);
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(pr.getId(), changesRequestedAt);
        responseTime.updateOnApproveAfterChanges(approvedAt);
        reviewResponseTimeRepository.save(responseTime);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertThat(response.reviewActivity().avgChangesResolutionMinutes()).isGreaterThan(0);
    }

    @Test
    void 기간_밖_리뷰와_응답시간은_통계에서_제외된다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr = createAndSavePullRequest(project.getId());

        createAndSaveReviewActivity(pr.getId(), ONE_INT, ONE_INT, FIFTY_INT, THIRTY_INT, false, ZERO_INT);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inRangeReviewedAt = now.minusHours(ONE_HOUR);
        LocalDateTime outOfRangeReviewedAt = now.minusDays(THIRTY_INT);

        createAndSaveReview(pr, inRangeReviewedAt, ReviewState.APPROVED);
        createAndSaveReview(pr, outOfRangeReviewedAt, ReviewState.CHANGES_REQUESTED);

        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(pr.getId(), outOfRangeReviewedAt);
        responseTime.updateOnApproveAfterChanges(outOfRangeReviewedAt.plusHours(TWO_HOURS));
        reviewResponseTimeRepository.save(responseTime);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(
                LocalDate.now().minusDays(ONE_DAY),
                LocalDate.now().plusDays(ONE_DAY)
        );

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.reviewActivity().changesRequestedRate()).isZero(),
                () -> assertThat(response.reviewActivity().firstReviewApproveRate()).isEqualTo(REVIEW_RATE_100),
                () -> assertThat(response.reviewActivity().avgChangesResolutionMinutes()).isZero()
        );
    }

    @Test
    void 리뷰_지표_비율을_계산한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveReviewActivity(pr1.getId(), ONE_INT, TWENTY_INT, FIFTY_INT, FIFTY_INT, false, TEN_INT);
        createAndSaveReviewActivity(pr2.getId(), ONE_INT, ONE_INT, HUNDRED_INT, HUNDRED_INT, false, ZERO_INT);

        LocalDateTime now = LocalDateTime.now();
        createAndSaveReview(pr1, now.minusMinutes(THIRTY_MINUTES), ReviewState.APPROVED);
        createAndSaveReview(pr2, now.minusMinutes(SIXTY_MINUTES), ReviewState.CHANGES_REQUESTED);
        createAndSaveReview(pr2, now.minusMinutes(THIRTY_MINUTES), ReviewState.APPROVED);

        ReviewQualityStatisticsRequest request = new ReviewQualityStatisticsRequest(null, null);

        // when
        ReviewQualityStatisticsResponse response = reviewQualityStatisticsQueryService
                .findReviewQualityStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.reviewActivity().highIntensityPrRate()).isEqualTo(REVIEW_RATE_50),
                () -> assertThat(response.reviewActivity().postReviewCommitRate()).isEqualTo(REVIEW_RATE_50),
                () -> assertThat(response.reviewActivity().firstReviewApproveRate()).isEqualTo(REVIEW_RATE_50),
                () -> assertThat(response.reviewActivity().changesRequestedRate()).isEqualTo(REVIEW_RATE_50)
        );
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create(TEST_PROJECT_NAME, TEST_API_KEY_PREFIX + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId) {
        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create(TEST_USER_NAME, USER_ID))
                .pullRequestNumber((int) (Math.abs(System.nanoTime() % PR_NUMBER_BOUND) + 1))
                .headCommitSha(TEST_HEAD_COMMIT_SHA)
                .title(TEST_PR_TITLE)
                .state(PullRequestState.MERGED)
                .link(TEST_PR_LINK)
                .changeStats(PullRequestChangeStats.create(
                        DEFAULT_CHANGE_STATS_ADDITIONS,
                        DEFAULT_CHANGE_STATS_DELETIONS,
                        DEFAULT_CHANGE_STATS_CHANGED_FILES
                ))
                .commitCount(DEFAULT_COMMIT_COUNT)
                .timing(PullRequestTiming.createOpen(LocalDateTime.now().minusDays(ONE_DAY)))
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private void createAndSaveReview(PullRequest pullRequest, LocalDateTime reviewedAt, ReviewState reviewState) {
        Review review = Review.builder()
                .githubPullRequestId(pullRequest.getGithubPullRequestId())
                .pullRequestNumber(pullRequest.getPullRequestNumber())
                .githubReviewId(System.nanoTime())
                .reviewer(GithubUser.create(FIRST_REVIEWER_NAME, FIRST_REVIEWER_ID))
                .reviewState(reviewState)
                .headCommitSha(TEST_HEAD_COMMIT_SHA)
                .body("LGTM")
                .commentCount(ONE_INT)
                .githubSubmittedAt(reviewedAt)
                .build();
        review.assignPullRequestId(pullRequest.getId());

        reviewRepository.save(review);
    }

    private void createAndSaveReviewActivity(
            Long pullRequestId,
            int reviewRoundTrips,
            int commentCount,
            int totalAdditions,
            int totalDeletions,
            boolean hasAdditionalReviewers,
            int codeChangesAfterReview
    ) {
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(pullRequestId)
                .reviewRoundTrips(reviewRoundTrips)
                .totalCommentCount(commentCount)
                .totalAdditions(totalAdditions)
                .totalDeletions(totalDeletions)
                .additionalReviewerCount(resolveAdditionalReviewerCount(hasAdditionalReviewers))
                .codeAdditionsAfterReview(codeChangesAfterReview)
                .codeDeletionsAfterReview(ZERO_INT)
                .build();

        reviewActivityRepository.save(activity);
    }

    private void createAndSaveReviewActivityWithoutReview(
            Long pullRequestId,
            int totalAdditions,
            int totalDeletions
    ) {
        ReviewActivity activity = ReviewActivity.createWithoutReview(pullRequestId, totalAdditions, totalDeletions);
        reviewActivityRepository.save(activity);
    }

    private void createAndSaveReviewSession(
            Long pullRequestId,
            Long reviewerGithubId,
            String reviewerName,
            Long sessionDurationMinutes,
            int reviewCount
    ) {
        LocalDateTime firstActivityAt = LocalDateTime.now().minusHours(TWO_HOURS);
        LocalDateTime lastActivityAt = firstActivityAt.plusMinutes(sessionDurationMinutes);

        ReviewSession session = ReviewSession.create(
                pullRequestId,
                GithubUser.create(reviewerName, reviewerGithubId),
                firstActivityAt
        );

        if (sessionDurationMinutes > ZERO_INT) {
            session.updateOnReview(lastActivityAt, ZERO_INT);
        }

        for (int i = TWO_INT; i < reviewCount; i++) {
            session.updateOnReview(lastActivityAt, ZERO_INT);
        }

        reviewSessionRepository.save(session);
    }

    private int resolveAdditionalReviewerCount(boolean hasAdditionalReviewers) {
        if (hasAdditionalReviewers) {
            return ONE_INT;
        }
        return ZERO_INT;
    }
}
