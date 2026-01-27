package com.prism.statistics.domain.pullrequest.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrStateTest {

    @Test
    void isDraft가_true이면_DRAFT를_반환한다() {
        // when
        PrState actual = PrState.create("open", false, true);

        // then
        assertThat(actual).isEqualTo(PrState.DRAFT);
    }

    @Test
    void state가_open이고_draft가_아니면_OPEN을_반환한다() {
        // when
        PrState actual = PrState.create("open", false, false);

        // then
        assertThat(actual).isEqualTo(PrState.OPEN);
    }

    @Test
    void state가_closed이고_merged가_true이면_MERGED를_반환한다() {
        // when
        PrState actual = PrState.create("closed", true, false);

        // then
        assertThat(actual).isEqualTo(PrState.MERGED);
    }

    @Test
    void state가_closed이고_merged가_false이면_CLOSED를_반환한다() {
        // when
        PrState actual = PrState.create("closed", false, false);

        // then
        assertThat(actual).isEqualTo(PrState.CLOSED);
    }

    @Test
    void state가_대소문자를_구분하지_않는다() {
        // when
        PrState openLower = PrState.create("open", false, false);
        PrState openUpper = PrState.create("OPEN", false, false);
        PrState closedMixed = PrState.create("Closed", true, false);

        // then
        assertAll(
                () -> assertThat(openLower).isEqualTo(PrState.OPEN),
                () -> assertThat(openUpper).isEqualTo(PrState.OPEN),
                () -> assertThat(closedMixed).isEqualTo(PrState.MERGED)
        );
    }

    @Test
    void 알_수_없는_state이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrState.create("unknown", false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("알 수 없는 PR 상태입니다: unknown");
    }

    @Test
    void isDraft가_true이면_state가_open이어도_DRAFT가_우선한다() {
        // when
        PrState actual = PrState.create("open", false, true);

        // then
        assertAll(
                () -> assertThat(actual).isEqualTo(PrState.DRAFT),
                () -> assertThat(actual).isNotEqualTo(PrState.OPEN)
        );
    }

    @ParameterizedTest
    @MethodSource("상태별_확인_메서드_테스트_데이터")
    void 각_상태는_자신만_true이고_나머지는_false를_반환한다(
            PrState state,
            boolean expectedDraft,
            boolean expectedOpen,
            boolean expectedMerged,
            boolean expectedClosed
    ) {
        // when & then
        assertAll(
                () -> assertThat(state.isDraft()).isEqualTo(expectedDraft),
                () -> assertThat(state.isOpen()).isEqualTo(expectedOpen),
                () -> assertThat(state.isMerged()).isEqualTo(expectedMerged),
                () -> assertThat(state.isClosed()).isEqualTo(expectedClosed)
        );
    }

    static Stream<Arguments> 상태별_확인_메서드_테스트_데이터() {
        return Stream.of(
                //          state,         isDraft, isOpen, isMerged, isClosed
                Arguments.of(PrState.DRAFT,  true,   false,  false,    false),
                Arguments.of(PrState.OPEN,   false,  true,   false,    false),
                Arguments.of(PrState.MERGED, false,  false,  true,     false),
                Arguments.of(PrState.CLOSED, false,  false,  false,    true)
        );
    }
}
