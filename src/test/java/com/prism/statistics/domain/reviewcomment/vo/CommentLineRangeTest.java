package com.prism.statistics.domain.reviewcomment.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CommentLineRangeTest {

    @Test
    void 단일_라인_댓글을_생성한다() {
        // when
        CommentLineRange lineRange = CommentLineRange.create(null, 10);

        // then
        assertAll(
                () -> assertThat(lineRange.getStartLine()).isNull(),
                () -> assertThat(lineRange.getEndLine()).isEqualTo(10),
                () -> assertThat(lineRange.isSingleLine()).isTrue(),
                () -> assertThat(lineRange.isMultiLine()).isFalse()
        );
    }

    @Test
    void 멀티_라인_댓글을_생성한다() {
        // when
        CommentLineRange lineRange = CommentLineRange.create(5, 10);

        // then
        assertAll(
                () -> assertThat(lineRange.getStartLine()).isEqualTo(5),
                () -> assertThat(lineRange.getEndLine()).isEqualTo(10),
                () -> assertThat(lineRange.isSingleLine()).isFalse(),
                () -> assertThat(lineRange.isMultiLine()).isTrue()
        );
    }

    @Test
    void endLine이_0보다_작으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CommentLineRange.create(null, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라인은 0보다 작을 수 없습니다.");
    }

    @Test
    void startLine이_0보다_작으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CommentLineRange.create(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라인은 0보다 작을 수 없습니다.");
    }

    @Test
    void startLine이_endLine보다_크거나_같으면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> CommentLineRange.create(10, 5))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("startLine은 endLine보다 작아야 합니다."),
                () -> assertThatThrownBy(() -> CommentLineRange.create(10, 10))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("startLine은 endLine보다 작아야 합니다.")
        );
    }
}
