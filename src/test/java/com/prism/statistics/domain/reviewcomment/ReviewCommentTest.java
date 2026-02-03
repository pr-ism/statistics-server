package com.prism.statistics.domain.reviewcomment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.prism.statistics.domain.reviewcomment.enums.CommentSide;
import com.prism.statistics.domain.reviewcomment.vo.CommentLineRange;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewCommentTest {

    private static final LocalDateTime GITHUB_CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime GITHUB_UPDATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void 리뷰_댓글을_생성한다() {
        // when
        ReviewComment comment = ReviewComment.create(
                1L, 100L,
                "코드 수정이 필요합니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(5, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        );

        // then
        assertAll(
                () -> assertThat(comment.getGithubCommentId()).isEqualTo(1L),
                () -> assertThat(comment.getGithubReviewId()).isEqualTo(100L),
                () -> assertThat(comment.getBody()).isEqualTo("코드 수정이 필요합니다."),
                () -> assertThat(comment.getPath()).isEqualTo("src/main/java/Example.java"),
                () -> assertThat(comment.getLineRange().getStartLine()).isEqualTo(5),
                () -> assertThat(comment.getLineRange().getEndLine()).isEqualTo(10),
                () -> assertThat(comment.getSide()).isEqualTo(CommentSide.RIGHT),
                () -> assertThat(comment.getCommitSha()).isEqualTo("abc123"),
                () -> assertThat(comment.getParentCommentId().hasParent()).isFalse(),
                () -> assertThat(comment.getAuthorMention()).isEqualTo("reviewer1"),
                () -> assertThat(comment.getAuthorGithubUid()).isEqualTo(12345L),
                () -> assertThat(comment.isDeleted()).isFalse()
        );
    }

    @Test
    void 단일_라인_댓글을_생성한다() {
        // when
        ReviewComment comment = ReviewComment.create(
                1L, 100L,
                "코드 리뷰입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        );

        // then
        assertAll(
                () -> assertThat(comment.getLineRange().isSingleLine()).isTrue(),
                () -> assertThat(comment.getLineRange().getEndLine()).isEqualTo(10)
        );
    }

    @Test
    void 부모_댓글이_있는_댓글을_생성한다() {
        // when
        ReviewComment comment = ReviewComment.create(
                2L, 100L,
                "답글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                1L,
                "reviewer2", 67890L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        );

        // then
        assertAll(
                () -> assertThat(comment.getParentCommentId().hasParent()).isTrue(),
                () -> assertThat(comment.getParentCommentId().getValue()).isEqualTo(1L)
        );
    }

    @Test
    void GitHub_Review_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, null,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub Review ID는 필수입니다.");
    }

    @Test
    void GitHub_Comment_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                null, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub Comment ID는 필수입니다.");
    }


    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 댓글_내용이_null이거나_빈_문자열이면_예외가_발생한다(String body) {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                body,
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 내용은 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 파일_경로가_null이거나_빈_문자열이면_예외가_발생한다(String path) {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                path,
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 경로는 필수입니다.");
    }

    @Test
    void lineRange가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                null,
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라인 범위는 필수입니다.");
    }

    @Test
    void side가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                null,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("CommentSide는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void commitSha가_null이거나_빈_문자열이면_예외가_발생한다(String commitSha) {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                commitSha,
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 SHA는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 작성자_멘션이_null이거나_빈_문자열이면_예외가_발생한다(String authorMention) {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                authorMention, 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자 멘션은 필수입니다.");
    }

    @Test
    void 작성자_GitHub_UID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", null,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자 GitHub UID는 필수입니다.");
    }

    @Test
    void GitHub_생성_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                null, GITHUB_UPDATED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub 생성 시각은 필수입니다.");
    }

    @Test
    void GitHub_수정_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub 수정 시각은 필수입니다.");
    }

    @Test
    void GitHub_생성_시각과_수정_시각이_다르면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 15, 11, 0);

        // when & then
        assertThatThrownBy(() -> ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                createdAt, updatedAt
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("생성 시 GitHub 생성 시각과 수정 시각은 동일해야 합니다.");
    }

    @Test
    void isOlderThan은_현재_수정_시각보다_이후_시간이면_true를_반환한다() {
        // given
        ReviewComment comment = ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        );
        LocalDateTime laterTime = GITHUB_UPDATED_AT.plusHours(1);

        // when & then
        assertThat(comment.isOlderThan(laterTime)).isTrue();
    }

    @Test
    void isOlderThan은_현재_수정_시각보다_이전_시간이면_false를_반환한다() {
        // given
        ReviewComment comment = ReviewComment.create(
                1L, 100L,
                "댓글입니다.",
                "src/main/java/Example.java",
                CommentLineRange.create(null, 10),
                CommentSide.RIGHT,
                "abc123",
                null,
                "reviewer1", 12345L,
                GITHUB_CREATED_AT, GITHUB_UPDATED_AT
        );
        LocalDateTime earlierTime = GITHUB_UPDATED_AT.minusHours(1);

        // when & then
        assertThat(comment.isOlderThan(earlierTime)).isFalse();
    }
}
