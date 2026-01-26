package com.prism.statistics.domain.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.prism.statistics.domain.pullrequest.enums.PrState;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrStateChangeHistoryTest {

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void 상태_변경_이력을_생성한다() {
        // when
        PrStateChangeHistory history = PrStateChangeHistory.create(
                1L, PrState.OPEN, PrState.MERGED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getPreviousState()).isEqualTo(PrState.OPEN),
                () -> assertThat(history.getNewState()).isEqualTo(PrState.MERGED),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT),
                () -> assertThat(history.isInitialState()).isFalse()
        );
    }

    @Test
    void 최초_상태_이력을_생성한다() {
        // when
        PrStateChangeHistory history = PrStateChangeHistory.createInitial(1L, PrState.OPEN, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getPreviousState()).isNull(),
                () -> assertThat(history.getNewState()).isEqualTo(PrState.OPEN),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT),
                () -> assertThat(history.isInitialState()).isTrue()
        );
    }

    @Test
    void 최초_상태가_DRAFT인_이력을_생성한다() {
        // when
        PrStateChangeHistory history = PrStateChangeHistory.createInitial(1L, PrState.DRAFT, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(history.getPreviousState()).isNull(),
                () -> assertThat(history.getNewState()).isEqualTo(PrState.DRAFT),
                () -> assertThat(history.isInitialState()).isTrue()
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrStateChangeHistory.create(null, PrState.OPEN, PrState.MERGED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @Test
    void 최초_상태_생성시_PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrStateChangeHistory.createInitial(null, PrState.OPEN, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @Test
    void 새로운_상태가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrStateChangeHistory.create(1L, PrState.OPEN, null, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 상태는 필수입니다.");
    }

    @Test
    void 최초_상태_생성시_상태가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrStateChangeHistory.createInitial(1L, null, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 상태는 필수입니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrStateChangeHistory.create(1L, PrState.OPEN, PrState.MERGED, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }

    @Test
    void 최초_상태_생성시_변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrStateChangeHistory.createInitial(1L, PrState.OPEN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
