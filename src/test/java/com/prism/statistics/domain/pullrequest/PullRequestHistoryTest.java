package com.prism.statistics.domain.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.prism.statistics.domain.pullrequest.vo.PrChangeStats;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestHistoryTest {

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void PullRequestHistory를_생성한다() {
        // given
        PrChangeStats changeStats = PrChangeStats.create(5, 100, 50);

        // when
        PullRequestHistory history = PullRequestHistory.create(1L, changeStats, 3, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getChangeStats()).isEqualTo(changeStats),
                () -> assertThat(history.getCommitCount()).isEqualTo(3),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // given
        PrChangeStats changeStats = PrChangeStats.create(5, 100, 50);

        // when & then
        assertThatThrownBy(() -> PullRequestHistory.create(null, changeStats, 3, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @Test
    void 변경된_값이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestHistory.create(1L, null, 3, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 내역은 필수입니다.");
    }

    @Test
    void 커밋_수가_음수이면_예외가_발생한다() {
        // given
        PrChangeStats changeStats = PrChangeStats.create(5, 100, 50);

        // when & then
        assertThatThrownBy(() -> PullRequestHistory.create(1L, changeStats, -1, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // given
        PrChangeStats changeStats = PrChangeStats.create(5, 100, 50);

        // when & then
        assertThatThrownBy(() -> PullRequestHistory.create(1L, changeStats, 3, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
