package com.prism.statistics.domain.reviewcomment.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prism.statistics.domain.analysis.metadata.review.enums.CommentSide;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CommentSideTest {

    @ParameterizedTest
    @ValueSource(strings = {"left", "LEFT", "Left"})
    void left에_대한_대소문자를_구분하지_않고_LEFT를_변환한다(String value) {
        // when
        CommentSide actual = CommentSide.from(value);

        // then
        assertThat(actual).isEqualTo(CommentSide.LEFT);
    }

    @ParameterizedTest
    @ValueSource(strings = {"right", "RIGHT", "Right"})
    void right에_대한_대소문자를_구분하지_않고_RIGHT를_변환한다(String value) {
        // when
        CommentSide actual = CommentSide.from(value);

        // then
        assertThat(actual).isEqualTo(CommentSide.RIGHT);
    }

    @Test
    void 알_수_없는_값이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CommentSide.from("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("알 수 없는 CommentSide입니다: unknown");
    }
}
