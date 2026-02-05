package com.prism.statistics.domain.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.review.Review;
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

    private static final LocalDateTime SUBMITTED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void APPROVED_상태로_리뷰를_생성한다() {
        // when
        Review review = Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.APPROVED,
                "abc123", "LGTM", 3, SUBMITTED_AT
        );

        // then
        assertAll(
                () -> assertThat(review.getGithubPullRequestId()).isEqualTo(1L),
                () -> assertThat(review.getGithubReviewId()).isEqualTo(100L),
                () -> assertThat(review.getGithubMention()).isEqualTo("reviewer1"),
                () -> assertThat(review.getGithubUid()).isEqualTo(12345L),
                () -> assertThat(review.getReviewState()).isEqualTo(ReviewState.APPROVED),
                () -> assertThat(review.getCommitSha()).isEqualTo("abc123"),
                () -> assertThat(review.getBody().getValue()).isEqualTo("LGTM"),
                () -> assertThat(review.getCommentCount()).isEqualTo(3),
                () -> assertThat(review.getSubmittedAt()).isEqualTo(SUBMITTED_AT)
        );
    }

    @Test
    void CHANGES_REQUESTED_상태로_리뷰를_생성한다() {
        // when
        Review review = Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.CHANGES_REQUESTED,
                "abc123", "수정이 필요합니다.", 2, SUBMITTED_AT
        );

        // then
        assertAll(
                () -> assertThat(review.getReviewState()).isEqualTo(ReviewState.CHANGES_REQUESTED),
                () -> assertThat(review.getBody().getValue()).isEqualTo("수정이 필요합니다.")
        );
    }

    @Test
    void COMMENTED_상태로_리뷰를_생성한다() {
        // when
        Review review = Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.COMMENTED,
                "abc123", "질문이 있습니다.", 1, SUBMITTED_AT
        );

        // then
        assertAll(
                () -> assertThat(review.getReviewState()).isEqualTo(ReviewState.COMMENTED),
                () -> assertThat(review.getBody().getValue()).isEqualTo("질문이 있습니다.")
        );
    }

    @Test
    void GitHub_PullRequest_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.create(
                null, 100L, "reviewer1", 12345L,
                ReviewState.APPROVED,
                "abc123", "LGTM", 3, SUBMITTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void GitHub_Review_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.create(
                1L, null, "reviewer1", 12345L,
                ReviewState.APPROVED,
                "abc123", "LGTM", 3, SUBMITTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub Review ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void GitHub_멘션이_null이거나_빈_문자열이면_예외가_발생한다(String githubMention) {
        // when & then
        assertThatThrownBy(() -> Review.create(
                1L, 100L, githubMention, 12345L,
                ReviewState.APPROVED,
                "abc123", "LGTM", 3, SUBMITTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub 멘션은 필수입니다.");
    }

    @Test
    void GitHub_UID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.create(
                1L, 100L, "reviewer1", null,
                ReviewState.APPROVED,
                "abc123", "LGTM", 3, SUBMITTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub UID는 필수입니다.");
    }

    @Test
    void 리뷰_상태가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.create(
                1L, 100L, "reviewer1", 12345L,
                null,
                "abc123", "LGTM", 3, SUBMITTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 상태는 필수입니다.");
    }

    @Test
    void 리뷰_제출_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.APPROVED,
                "abc123", "LGTM", 3, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 제출 시각은 필수입니다.");
    }

    @Test
    void APPROVED_상태에서_body가_null이어도_리뷰를_생성할_수_있다() {
        // when
        Review review = Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.APPROVED,
                "abc123", null, 0, SUBMITTED_AT
        );

        // then
        assertThat(review.getBody().isEmpty()).isTrue();
    }

    @Test
    void CHANGES_REQUESTED_상태에서_body가_null이어도_리뷰를_생성할_수_있다() {
        // when
        Review review = Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.CHANGES_REQUESTED,
                "abc123", null, 0, SUBMITTED_AT
        );

        // then
        assertThat(review.getBody().isEmpty()).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void COMMENTED_상태에서_body가_null이거나_빈_문자열이면_예외가_발생한다(String body) {
        // when & then
        assertThatThrownBy(() -> Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.COMMENTED,
                "abc123", body, 0, SUBMITTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 본문은 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void commitSha가_null이거나_빈_문자열이면_예외가_발생한다(String commitSha) {
        // when & then
        assertThatThrownBy(() -> Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.APPROVED,
                commitSha, "LGTM", 0, SUBMITTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 SHA는 필수입니다.");
    }

    @Test
    void 댓글_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Review.create(
                1L, 100L, "reviewer1", 12345L,
                ReviewState.APPROVED,
                "abc123", "LGTM", -1, SUBMITTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 수는 0 이상이어야 합니다.");
    }
}
