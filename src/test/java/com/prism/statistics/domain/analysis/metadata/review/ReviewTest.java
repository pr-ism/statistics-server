package com.prism.statistics.domain.analysis.metadata.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewState;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewTest {

    private static final LocalDateTime GITHUB_SUBMITTED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final GithubUser REVIEWER = GithubUser.create("reviewer1", 12345L);

    @Test
    void APPROVED_상태로_리뷰를_생성한다() {
        // when
        Review review = Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(3)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build();

        // then
        assertAll(
                () -> assertThat(review.getPullRequestId()).isNull(),
                () -> assertThat(review.getGithubPullRequestId()).isEqualTo(1L),
                () -> assertThat(review.getGithubReviewId()).isEqualTo(100L),
                () -> assertThat(review.getReviewer()).isEqualTo(REVIEWER),
                () -> assertThat(review.getReviewState()).isEqualTo(ReviewState.APPROVED),
                () -> assertThat(review.getHeadCommitSha()).isEqualTo("abc123"),
                () -> assertThat(review.getBody().getValue()).isEqualTo("LGTM"),
                () -> assertThat(review.getCommentCount()).isEqualTo(3),
                () -> assertThat(review.getGithubSubmittedAt()).isEqualTo(GITHUB_SUBMITTED_AT)
        );
    }

    @Test
    void CHANGES_REQUESTED_상태로_리뷰를_생성한다() {
        // when
        Review review = Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.CHANGES_REQUESTED)
                .headCommitSha("abc123")
                .body("수정이 필요합니다.")
                .commentCount(2)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build();

        // then
        assertAll(
                () -> assertThat(review.getReviewState()).isEqualTo(ReviewState.CHANGES_REQUESTED),
                () -> assertThat(review.getBody().getValue()).isEqualTo("수정이 필요합니다.")
        );
    }

    @Test
    void COMMENTED_상태로_리뷰를_생성한다() {
        // when
        Review review = Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.COMMENTED)
                .headCommitSha("abc123")
                .body("질문이 있습니다.")
                .commentCount(1)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build();

        // then
        assertAll(
                () -> assertThat(review.getReviewState()).isEqualTo(ReviewState.COMMENTED),
                () -> assertThat(review.getBody().getValue()).isEqualTo("질문이 있습니다.")
        );
    }

    @Test
    void GitHub_PullRequest_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.builder()
                .githubPullRequestId(null)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(3)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void GitHub_Review_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(null)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(3)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub Review ID는 필수입니다.");
    }

    @Test
    void 리뷰어가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(null)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(3)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어는 필수입니다.");
    }

    @Test
    void 리뷰_상태가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(null)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(3)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 상태는 필수입니다.");
    }

    @Test
    void 리뷰_제출_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(3)
                .githubSubmittedAt(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 제출 시각은 필수입니다.");
    }

    @Test
    void APPROVED_상태에서_body가_null이어도_리뷰를_생성할_수_있다() {
        // when
        Review review = Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body(null)
                .commentCount(0)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build();

        // then
        assertThat(review.getBody().isEmpty()).isTrue();
    }

    @Test
    void CHANGES_REQUESTED_상태에서_body가_null이어도_리뷰를_생성할_수_있다() {
        // when
        Review review = Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.CHANGES_REQUESTED)
                .headCommitSha("abc123")
                .body(null)
                .commentCount(0)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build();

        // then
        assertThat(review.getBody().isEmpty()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void COMMENTED_상태에서_body가_null이거나_빈_문자열이면_예외가_발생한다(String body) {
        // when & then
        assertThatThrownBy(() -> Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.COMMENTED)
                .headCommitSha("abc123")
                .body(body)
                .commentCount(0)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 본문은 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 헤드_커밋_SHA가_null이거나_빈_문자열이면_예외가_발생한다(String headCommitSha) {
        // when & then
        assertThatThrownBy(() -> Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha(headCommitSha)
                .body("LGTM")
                .commentCount(0)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("헤드 커밋 SHA는 필수입니다.");
    }

    @Test
    void 댓글_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(-1)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 수는 0 이상이어야 합니다.");
    }

    @Test
    void assignPullRequestId로_pullRequestId를_할당한다() {
        // given
        Review review = Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(0)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build();

        // when
        review.assignPullRequestId(1L);

        // then
        assertThat(review.getPullRequestId()).isEqualTo(1L);
    }

    @Test
    void pullRequestId가_이미_할당되어_있으면_덮어쓰지_않는다() {
        // given
        Review review = Review.builder()
                .githubPullRequestId(1L)
                .githubReviewId(100L)
                .reviewer(REVIEWER)
                .reviewState(ReviewState.APPROVED)
                .headCommitSha("abc123")
                .body("LGTM")
                .commentCount(0)
                .githubSubmittedAt(GITHUB_SUBMITTED_AT)
                .build();
        review.assignPullRequestId(1L);

        // when
        review.assignPullRequestId(999L);

        // then
        assertThat(review.getPullRequestId()).isEqualTo(1L);
    }
}
