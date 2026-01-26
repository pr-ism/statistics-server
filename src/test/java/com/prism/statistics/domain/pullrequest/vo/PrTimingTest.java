package com.prism.statistics.domain.pullrequest.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrTimingTest {

    @Test
    void createOpen으로_OPEN_상태의_시간_정보를_생성한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when
        PrTiming timing = PrTiming.createOpen(createdAt);

        // then
        assertThat(timing.getPrCreatedAt()).isEqualTo(createdAt);
        assertThat(timing.getMergedAt()).isNull();
        assertThat(timing.getClosedAt()).isNull();
    }

    @Test
    void createOpen에서_생성_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrTiming.createOpen(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR 생성 시각은 필수입니다.");
    }

    @Test
    void createDraft로_DRAFT_상태의_시간_정보를_생성한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when
        PrTiming timing = PrTiming.createDraft(createdAt);

        // then
        assertThat(timing.getPrCreatedAt()).isEqualTo(createdAt);
        assertThat(timing.getMergedAt()).isNull();
        assertThat(timing.getClosedAt()).isNull();
    }

    @Test
    void createDraft에서_생성_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrTiming.createDraft(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR 생성 시각은 필수입니다.");
    }

    @Test
    void createMerged로_MERGED_상태의_시간_정보를_생성한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when
        PrTiming timing = PrTiming.createMerged(createdAt, mergedAt, closedAt);

        // then
        assertThat(timing.getPrCreatedAt()).isEqualTo(createdAt);
        assertThat(timing.getMergedAt()).isEqualTo(mergedAt);
        assertThat(timing.getClosedAt()).isEqualTo(closedAt);
    }

    @Test
    void createMerged에서_생성_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when & then
        assertThatThrownBy(() -> PrTiming.createMerged(null, mergedAt, closedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR 생성 시각은 필수입니다.");
    }

    @Test
    void createMerged에서_병합_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when & then
        assertThatThrownBy(() -> PrTiming.createMerged(createdAt, null, closedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("병합 시각은 필수입니다.");
    }

    @Test
    void createMerged에서_종료_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when & then
        assertThatThrownBy(() -> PrTiming.createMerged(createdAt, mergedAt, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시각은 필수입니다.");
    }

    @Test
    void createMerged에서_생성_시각이_병합_시각보다_이후면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 14, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 14, 0);

        // when & then
        assertThatThrownBy(() -> PrTiming.createMerged(createdAt, mergedAt, closedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("병합 시각은 생성 시각 이후여야 합니다.");
    }

    @Test
    void createMerged에서_생성_시각이_종료_시각보다_이후면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 14, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 15, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when & then
        assertThatThrownBy(() -> PrTiming.createMerged(createdAt, mergedAt, closedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시각은 생성 시각 이후여야 합니다.");
    }

    @Test
    void createClosed로_CLOSED_상태의_시간_정보를_생성한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 14, 0);

        // when
        PrTiming timing = PrTiming.createClosed(createdAt, closedAt);

        // then
        assertThat(timing.getPrCreatedAt()).isEqualTo(createdAt);
        assertThat(timing.getMergedAt()).isNull();
        assertThat(timing.getClosedAt()).isEqualTo(closedAt);
    }

    @Test
    void createClosed에서_생성_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 14, 0);

        // when & then
        assertThatThrownBy(() -> PrTiming.createClosed(null, closedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR 생성 시각은 필수입니다.");
    }

    @Test
    void createClosed에서_종료_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when & then
        assertThatThrownBy(() -> PrTiming.createClosed(createdAt, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시각은 필수입니다.");
    }

    @Test
    void createClosed에서_생성_시각이_종료_시각보다_이후면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 16, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 14, 0);

        // when & then
        assertThatThrownBy(() -> PrTiming.createClosed(createdAt, closedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시각은 생성 시각 이후여야 합니다.");
    }

    @Test
    void calculateMergeTimeMinutes는_병합_시간을_분_단위로_계산한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 30);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 12, 30);

        PrTiming timing = PrTiming.createMerged(createdAt, mergedAt, closedAt);

        // when
        long mergeTimeMinutes = timing.calculateMergeTimeMinutes();

        // then
        assertThat(mergeTimeMinutes).isEqualTo(150);
    }

    @Test
    void calculateMergeTimeMinutes는_병합되지_않은_PR이면_예외가_발생한다() {
        // given
        PrTiming timing = PrTiming.createOpen(LocalDateTime.of(2024, 1, 15, 10, 0));

        // when & then
        assertThatThrownBy(timing::calculateMergeTimeMinutes)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("병합되지 않은 PR입니다.");
    }

    @Test
    void 동등성을_비교한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        PrTiming timing1 = PrTiming.createMerged(createdAt, mergedAt, closedAt);
        PrTiming timing2 = PrTiming.createMerged(createdAt, mergedAt, closedAt);

        // then
        assertThat(timing1).isEqualTo(timing2);
    }
}
