package com.prism.statistics.domain.reviewcomment.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ParentCommentIdTest {

    @Test
    void 부모_댓글_ID를_생성한다() {
        // when
        ParentCommentId parentCommentId = ParentCommentId.create(123L);

        // then
        assertAll(
                () -> assertThat(parentCommentId.getValue()).isEqualTo(123L),
                () -> assertThat(parentCommentId.hasParent()).isTrue()
        );
    }

    @Test
    void null로_생성하면_부모가_없는_상태가_된다() {
        // when
        ParentCommentId parentCommentId = ParentCommentId.create(null);

        // then
        assertAll(
                () -> assertThat(parentCommentId.getValue()).isNull(),
                () -> assertThat(parentCommentId.hasParent()).isFalse()
        );
    }

    @Test
    void 댓글_ID가_0이하이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> ParentCommentId.create(0L))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("댓글 ID는 양수여야 합니다."),
                () -> assertThatThrownBy(() -> ParentCommentId.create(-1L))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("댓글 ID는 양수여야 합니다.")
        );
    }
}
