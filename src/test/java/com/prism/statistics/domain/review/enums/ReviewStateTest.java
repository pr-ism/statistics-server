package com.prism.statistics.domain.review.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewStateTest {

    @ParameterizedTest
    @ValueSource(strings = {"approved", "APPROVED", "Approved"})
    void APPROVED_상태를_변환한다(String value) {
        // when
        ReviewState actual = ReviewState.from(value);

        // then
        assertThat(actual).isEqualTo(ReviewState.APPROVED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"changes_requested", "CHANGES_REQUESTED", "Changes_Requested"})
    void CHANGES_REQUESTED_상태를_변환한다(String value) {
        // when
        ReviewState actual = ReviewState.from(value);

        // then
        assertThat(actual).isEqualTo(ReviewState.CHANGES_REQUESTED);
    }

    @ParameterizedTest
    @ValueSource(strings = {"commented", "COMMENTED", "Commented"})
    void COMMENTED_상태를_변환한다(String value) {
        // when
        ReviewState actual = ReviewState.from(value);

        // then
        assertThat(actual).isEqualTo(ReviewState.COMMENTED);
    }

    @Test
    void 알_수_없는_상태이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewState.from("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("알 수 없는 리뷰 상태입니다: unknown");
    }
}
