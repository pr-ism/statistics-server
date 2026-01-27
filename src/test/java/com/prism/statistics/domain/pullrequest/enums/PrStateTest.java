package com.prism.statistics.domain.pullrequest.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

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

    @Test
    void DRAFT_상태인지_확인한다() {
        // given
        PrState draft = PrState.DRAFT;

        // when
        boolean actual = draft.isDraft();

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void OPEN_상태인지_확인한다() {
        // given
        PrState open = PrState.OPEN;

        // when
        boolean actual = open.isOpen();

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void MERGED_상태인지_확인한다() {
        // given
        PrState merged = PrState.MERGED;

        // when
        boolean actual = merged.isMerged();

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void CLOSED_상태인지_확인한다() {
        // given
        PrState closed = PrState.CLOSED;

        // when
        boolean actual = closed.isClosed();

        // then
        assertThat(actual).isTrue();
    }

    @Test
    void 다른_상태에서는_false를_반환한다() {
        // given
        PrState open = PrState.OPEN;

        // when & then
        assertAll(
                () -> assertThat(open.isDraft()).isFalse(),
                () -> assertThat(open.isMerged()).isFalse(),
                () -> assertThat(open.isClosed()).isFalse()
        );
    }
}
