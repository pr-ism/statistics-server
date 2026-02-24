package com.prism.statistics.application.statistics;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.statistics.dto.request.PullRequestSizeStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.PullRequestSizeStatisticsResponse;
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
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

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
class PullRequestSizeStatisticsQueryServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;
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
    private static final int FIVE_INT = 5;
    private static final int TEN_INT = 10;
    private static final int TWENTY_INT = 20;
    private static final int FIFTY_INT = 50;
    private static final int ONE_HUNDRED_INT = 100;
    private static final int TWO_HUNDRED_INT = 200;
    private static final int FIVE_HUNDRED_INT = 500;
    private static final long SIXTY_MINUTES = 60L;
    private static final long ONE_HUNDRED_TWENTY_MINUTES = 120L;
    private static final long THIRTY_MINUTES = 30L;
    private static final long REVIEW_WAIT_INCREMENT = 30L;
    private static final int CORRELATION_SAMPLE_SIZE = 5;
    private static final double LARGE_PR_RATE = 50.0;
    private static final double CORRELATION_EPSILON = 0.01;
    private static final long ZERO_COUNT = 0L;
    private static final long ONE_COUNT = 1L;
    private static final long TWO_COUNT = 2L;
    private static final BigDecimal FILE_CHANGE_DIVERSITY = BigDecimal.valueOf(0.3);
    private static final String INTERPRETATION_NO_DATA = "데이터 부족";

    @Autowired
    private PullRequestSizeStatisticsQueryService pullRequestSizeStatisticsQueryService;

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
        Project project = createAndSaveProject(USER_ID);

        PullRequest pr1 = createAndSavePullRequest(project.getId());
        PullRequest pr2 = createAndSavePullRequest(project.getId());

        createAndSaveSize(pr1.getId(), FIFTY_INT, TWENTY_INT, THREE_INT);
        createAndSaveSize(pr2.getId(), TWO_HUNDRED_INT, ONE_HUNDRED_INT, TEN_INT);

        createAndSaveBottleneck(pr1.getId(), SIXTY_MINUTES);
        createAndSaveBottleneck(pr2.getId(), ONE_HUNDRED_TWENTY_MINUTES);

        createAndSaveReviewActivity(pr1.getId(), ONE_INT);
        createAndSaveReviewActivity(pr2.getId(), THREE_INT);

        PullRequestSizeStatisticsRequest request = new PullRequestSizeStatisticsRequest(null, null);

        // when
        PullRequestSizeStatisticsResponse response = pullRequestSizeStatisticsQueryService
                .findPullRequestSizeStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isEqualTo(TWO_COUNT),
                () -> assertThat(response.avgSizeScore()).isGreaterThan(0),
                () -> assertThat(response.sizeGradeDistribution()).isNotEmpty()
        );
    }

    @Test
    void 데이터가_없으면_빈_통계를_반환한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequestSizeStatisticsRequest request = new PullRequestSizeStatisticsRequest(null, null);

        // when
        PullRequestSizeStatisticsResponse response = pullRequestSizeStatisticsQueryService
                .findPullRequestSizeStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.totalPullRequestCount()).isZero(),
                () -> assertThat(response.avgSizeScore()).isZero(),
                () -> assertThat(response.largePullRequestRate()).isZero()
        );
    }

    @Test
    void 날짜_범위로_필터링하여_통계를_조회한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr.getId(), FIFTY_INT, TWENTY_INT, THREE_INT);

        LocalDate startDate = LocalDate.now().minusDays(DEFAULT_DAYS_RANGE);
        LocalDate endDate = LocalDate.now().plusDays(ONE_DAY);
        PullRequestSizeStatisticsRequest request = new PullRequestSizeStatisticsRequest(startDate, endDate);

        // when
        PullRequestSizeStatisticsResponse response = pullRequestSizeStatisticsQueryService
                .findPullRequestSizeStatistics(USER_ID, project.getId(), request);

        // then
        assertThat(response.totalPullRequestCount()).isEqualTo(ONE_COUNT);
    }

    @Test
    void 소유하지_않은_프로젝트_통계_조회시_예외가_발생한다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequestSizeStatisticsRequest request = new PullRequestSizeStatisticsRequest(null, null);

        // when & then
        assertThatThrownBy(() ->
                pullRequestSizeStatisticsQueryService.findPullRequestSizeStatistics(OTHER_USER_ID, project.getId(), request)
        ).isInstanceOf(ProjectOwnershipException.class);
    }

    @Test
    void 모든_크기_등급이_분포에_포함된다() {
        // given
        Project project = createAndSaveProject(USER_ID);
        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr.getId(), FIFTY_INT, TWENTY_INT, THREE_INT);

        PullRequestSizeStatisticsRequest request = new PullRequestSizeStatisticsRequest(null, null);

        // when
        PullRequestSizeStatisticsResponse response = pullRequestSizeStatisticsQueryService
                .findPullRequestSizeStatistics(USER_ID, project.getId(), request);

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
        Project project = createAndSaveProject(USER_ID);

        // 작은 PR
        PullRequest pr1 = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr1.getId(), TEN_INT, FIVE_INT, ONE_INT);

        // 큰 PR (L 이상)
        PullRequest pr2 = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr2.getId(), FIVE_HUNDRED_INT, TWO_HUNDRED_INT, TWENTY_INT);

        PullRequestSizeStatisticsRequest request = new PullRequestSizeStatisticsRequest(null, null);

        // when
        PullRequestSizeStatisticsResponse response = pullRequestSizeStatisticsQueryService
                .findPullRequestSizeStatistics(USER_ID, project.getId(), request);

        // then
        assertThat(response.largePullRequestRate()).isCloseTo(LARGE_PR_RATE, within(CORRELATION_EPSILON));
    }

    @Test
    void 상관관계_데이터가_충분하면_상관계수를_계산한다() {
        // given
        Project project = createAndSaveProject(USER_ID);

        for (int i = 0; i < CORRELATION_SAMPLE_SIZE; i++) {
            PullRequest pr = createAndSavePullRequest(project.getId());
            createAndSaveSize(
                    pr.getId(),
                    (i + 1) * FIFTY_INT,
                    (i + 1) * TWENTY_INT,
                    (i + 1) * TWO_INT
            );
            createAndSaveBottleneck(pr.getId(), (i + 1) * REVIEW_WAIT_INCREMENT);
            createAndSaveReviewActivity(pr.getId(), i + 1);
        }

        PullRequestSizeStatisticsRequest request = new PullRequestSizeStatisticsRequest(null, null);

        // when
        PullRequestSizeStatisticsResponse response = pullRequestSizeStatisticsQueryService
                .findPullRequestSizeStatistics(USER_ID, project.getId(), request);

        // then
        assertAll(
                () -> assertThat(response.sizeReviewWaitCorrelation().interpretation()).isNotEqualTo(INTERPRETATION_NO_DATA),
                () -> assertThat(response.sizeReviewRoundTripCorrelation().interpretation()).isNotEqualTo(INTERPRETATION_NO_DATA)
        );
    }

    @Test
    void 상관관계_데이터가_부족하면_데이터_부족으로_표시된다() {
        // given
        Project project = createAndSaveProject(USER_ID);

        PullRequest pr = createAndSavePullRequest(project.getId());
        createAndSaveSize(pr.getId(), FIFTY_INT, TWENTY_INT, THREE_INT);

        PullRequestSizeStatisticsRequest request = new PullRequestSizeStatisticsRequest(null, null);

        // when
        PullRequestSizeStatisticsResponse response = pullRequestSizeStatisticsQueryService
                .findPullRequestSizeStatistics(USER_ID, project.getId(), request);

        // then
        assertThat(response.sizeReviewWaitCorrelation().interpretation()).isEqualTo(INTERPRETATION_NO_DATA);
    }

    @Test
    void 전체_건수가_0이면_퍼센트는_0이다() {
        double result = ReflectionTestUtils.invokeMethod(
                pullRequestSizeStatisticsQueryService,
                "calculatePercentage",
                ONE_COUNT,
                ZERO_COUNT
        );

        assertThat(result).isZero();
    }

    private Project createAndSaveProject(Long userId) {
        Project project = Project.create(TEST_PROJECT_NAME, TEST_API_KEY_PREFIX + System.nanoTime(), userId);
        return projectRepository.save(project);
    }

    private PullRequest createAndSavePullRequest(Long projectId) {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);

        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(System.nanoTime())
                .projectId(projectId)
                .author(GithubUser.create(TEST_USER_NAME, USER_ID))
                .pullRequestNumber((int) (Math.abs(System.nanoTime() % PR_NUMBER_BOUND)) + 1)
                .headCommitSha(TEST_HEAD_COMMIT_SHA)
                .title(TEST_PR_TITLE)
                .state(PullRequestState.OPEN)
                .link(TEST_PR_LINK)
                .changeStats(PullRequestChangeStats.create(
                        DEFAULT_CHANGE_STATS_ADDITIONS,
                        DEFAULT_CHANGE_STATS_DELETIONS,
                        DEFAULT_CHANGE_STATS_CHANGED_FILES
                ))
                .commitCount(DEFAULT_COMMIT_COUNT)
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
                FILE_CHANGE_DIVERSITY
        );
        sizeRepository.save(size);
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

    private void createAndSaveReviewActivity(Long pullRequestId, int roundTrips) {
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(pullRequestId)
                .reviewRoundTrips(roundTrips)
                .totalCommentCount(roundTrips * TWO_INT)
                .codeAdditionsAfterReview(ZERO_INT)
                .codeDeletionsAfterReview(ZERO_INT)
                .additionalReviewerCount(ZERO_INT)
                .totalAdditions(ONE_HUNDRED_INT)
                .totalDeletions(FIFTY_INT)
                .build();
        reviewActivityRepository.save(activity);
    }
}
