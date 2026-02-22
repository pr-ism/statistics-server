package com.prism.statistics.application.analysis.metadata.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest.ReviewerData;
import com.prism.statistics.domain.analysis.metadata.review.Review;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.infrastructure.analysis.metadata.review.persistence.JpaReviewRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewSubmittedServiceTest {

    private static final String TEST_API_KEY = "test-api-key";
    private static final Instant TEST_SUBMITTED_AT = Instant.parse("2024-01-15T10:00:00Z");
    private static final LocalDateTime EXPECTED_SUBMITTED_AT = LocalDateTime.of(2024, 1, 15, 19, 0, 0);

    @Autowired
    private ReviewSubmittedService reviewSubmittedService;

    @Autowired
    private JpaReviewRepository jpaReviewRepository;

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 리뷰_제출_시_Review가_저장된다() {
        // given
        ReviewSubmittedRequest request = createReviewSubmittedRequest(100L, "approved");

        // when
        reviewSubmittedService.submitReview(TEST_API_KEY, request);

        // then
        assertThat(jpaReviewRepository.count()).isEqualTo(1);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 리뷰_제출_시_Review_정보가_올바르게_저장된다() {
        // given
        Long githubReviewId = 100L;
        String reviewerLogin = "reviewer1";
        Long reviewerId = 12345L;
        ReviewSubmittedRequest request = createReviewSubmittedRequest(
                1L, 123, githubReviewId, reviewerLogin, reviewerId,
                "approved", "abc123sha", "LGTM", 3
        );

        // when
        reviewSubmittedService.submitReview(TEST_API_KEY, request);

        // then
        List<Review> reviews = jpaReviewRepository.findAll();
        assertThat(reviews).hasSize(1);

        Review review = reviews.get(0);
        assertAll(
                () -> assertThat(review.getGithubPullRequestId()).isEqualTo(1L),
                () -> assertThat(review.getGithubReviewId()).isEqualTo(githubReviewId),
                () -> assertThat(review.getReviewer().getUserName()).isEqualTo(reviewerLogin),
                () -> assertThat(review.getReviewer().getUserId()).isEqualTo(reviewerId),
                () -> assertThat(review.getReviewState()).isEqualTo(ReviewState.APPROVED),
                () -> assertThat(review.getHeadCommitSha()).isEqualTo("abc123sha"),
                () -> assertThat(review.getBody().getValue()).isEqualTo("LGTM"),
                () -> assertThat(review.getCommentCount()).isEqualTo(3),
                () -> assertThat(review.getGithubSubmittedAt()).isEqualTo(EXPECTED_SUBMITTED_AT)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void CHANGES_REQUESTED_상태로_리뷰를_저장한다() {
        // given
        ReviewSubmittedRequest request = createReviewSubmittedRequest(100L, "changes_requested");

        // when
        reviewSubmittedService.submitReview(TEST_API_KEY, request);

        // then
        Review review = jpaReviewRepository.findAll().get(0);
        assertThat(review.getReviewState()).isEqualTo(ReviewState.CHANGES_REQUESTED);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void COMMENTED_상태로_리뷰를_저장한다() {
        // given
        ReviewSubmittedRequest request = createReviewSubmittedRequest(
                1L, 123, 100L, "reviewer1", 12345L,
                "commented", "abc123sha", "질문이 있습니다.", 1
        );

        // when
        reviewSubmittedService.submitReview(TEST_API_KEY, request);

        // then
        Review review = jpaReviewRepository.findAll().get(0);
        assertAll(
                () -> assertThat(review.getReviewState()).isEqualTo(ReviewState.COMMENTED),
                () -> assertThat(review.getBody().getValue()).isEqualTo("질문이 있습니다.")
        );
    }

    @Sql("/sql/webhook/insert_project_and_pull_request.sql")
    @Test
    void PullRequest가_존재하면_pullRequestId가_할당된다() {
        // given
        ReviewSubmittedRequest request = createReviewSubmittedRequest(
                1001L, 123, 100L, "reviewer1", 12345L,
                "approved", "abc123sha", "LGTM", 0
        );

        // when
        reviewSubmittedService.submitReview(TEST_API_KEY, request);

        // then
        Review review = jpaReviewRepository.findAll().getFirst();
        assertThat(review.getPullRequestId()).isEqualTo(1L);
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 중복_리뷰_제출_시_저장되지_않는다() {
        // given
        Long sameGithubReviewId = 100L;
        ReviewSubmittedRequest request = createReviewSubmittedRequest(sameGithubReviewId, "approved");

        // when
        reviewSubmittedService.submitReview(TEST_API_KEY, request);
        reviewSubmittedService.submitReview(TEST_API_KEY, request);

        // then
        assertThat(jpaReviewRepository.count()).isEqualTo(1);
    }

    @Test
    void 존재하지_않는_API_Key면_예외가_발생한다() {
        // given
        ReviewSubmittedRequest request = createReviewSubmittedRequest(100L, "approved");
        String invalidApiKey = "invalid-api-key";

        // when & then
        assertThatThrownBy(() -> reviewSubmittedService.submitReview(invalidApiKey, request))
                .isInstanceOf(InvalidApiKeyException.class);
    }

    private ReviewSubmittedRequest createReviewSubmittedRequest(Long githubReviewId, String state) {
        return createReviewSubmittedRequest(
                1L, 123, githubReviewId, "reviewer1", 12345L,
                state, "abc123sha", "LGTM", 0
        );
    }

    private ReviewSubmittedRequest createReviewSubmittedRequest(
            Long githubPullRequestId,
            int pullRequestNumber,
            Long githubReviewId,
            String reviewerLogin,
            Long reviewerId,
            String state,
            String commitSha,
            String body,
            int commentCount
    ) {
        return new ReviewSubmittedRequest(
                githubPullRequestId,
                pullRequestNumber,
                githubReviewId,
                new ReviewerData(reviewerLogin, reviewerId),
                state,
                commitSha,
                body,
                commentCount,
                TEST_SUBMITTED_AT
        );
    }
}
