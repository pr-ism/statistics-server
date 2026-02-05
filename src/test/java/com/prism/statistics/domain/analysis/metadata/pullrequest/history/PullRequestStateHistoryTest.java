package com.prism.statistics.domain.analysis.metadata.pullrequest.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestStateHistoryTest {

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void 상태_변경_이력을_생성한다() {
        // when
        PullRequestStateHistory history = PullRequestStateHistory.create(
                1L, PullRequestState.OPEN, PullRequestState.MERGED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getPreviousState()).isEqualTo(PullRequestState.OPEN),
                () -> assertThat(history.getNewState()).isEqualTo(PullRequestState.MERGED),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT),
                () -> assertThat(history.isInitialState()).isFalse()
        );
    }

    @Test
    void 최초_상태_이력을_생성한다() {
        // when
        PullRequestStateHistory history = PullRequestStateHistory.createInitial(1L, PullRequestState.OPEN, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getPreviousState()).isNull(),
                () -> assertThat(history.getNewState()).isEqualTo(PullRequestState.OPEN),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT),
                () -> assertThat(history.isInitialState()).isTrue()
        );
    }

    @Test
    void 최초_상태가_DRAFT인_이력을_생성한다() {
        // when
        PullRequestStateHistory history = PullRequestStateHistory.createInitial(1L, PullRequestState.DRAFT, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(history.getPreviousState()).isNull(),
                () -> assertThat(history.getNewState()).isEqualTo(PullRequestState.DRAFT),
                () -> assertThat(history.isInitialState()).isTrue()
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestStateHistory.create(null, PullRequestState.OPEN, PullRequestState.MERGED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @Test
    void 최초_상태_생성시_PullRequest_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestStateHistory.createInitial(null, PullRequestState.OPEN, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @Test
    void 새로운_상태가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestStateHistory.create(1L, PullRequestState.OPEN, null, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 상태는 필수입니다.");
    }

    @Test
    void 최초_상태_생성시_상태가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestStateHistory.createInitial(1L, null, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("새로운 상태는 필수입니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestStateHistory.create(1L, PullRequestState.OPEN, PullRequestState.MERGED, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }

    @Test
    void 최초_상태_생성시_변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestStateHistory.createInitial(1L, PullRequestState.OPEN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
