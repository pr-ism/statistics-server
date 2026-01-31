package com.prism.statistics.domain.pullrequest.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestTimingTest {

    @Test
    void createOpen으로_OPEN_상태의_시간_정보를_생성한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when
        PullRequestTiming pullRequestTiming = PullRequestTiming.createOpen(createdAt);

        // then
        assertAll(
                () -> assertThat(pullRequestTiming.getPullRequestCreatedAt()).isEqualTo(createdAt),
                () -> assertThat(pullRequestTiming.getMergedAt()).isNull(),
                () -> assertThat(pullRequestTiming.getClosedAt()).isNull()
        );
    }

    @Test
    void createOpen에서_생성_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestTiming.createOpen(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 생성 시각은 필수입니다.");
    }

    @Test
    void createDraft로_DRAFT_상태의_시간_정보를_생성한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when
        PullRequestTiming pullRequestTiming = PullRequestTiming.createDraft(createdAt);

        // then
        assertAll(
                () -> assertThat(pullRequestTiming.getPullRequestCreatedAt()).isEqualTo(createdAt),
                () -> assertThat(pullRequestTiming.getMergedAt()).isNull(),
                () -> assertThat(pullRequestTiming.getClosedAt()).isNull()
        );
    }

    @Test
    void createDraft에서_생성_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestTiming.createDraft(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 생성 시각은 필수입니다.");
    }

    @Test
    void createMerged로_MERGED_상태의_시간_정보를_생성한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when
        PullRequestTiming pullRequestTiming = PullRequestTiming.createMerged(createdAt, mergedAt);

        // then
        assertAll(
                () -> assertThat(pullRequestTiming.getPullRequestCreatedAt()).isEqualTo(createdAt),
                () -> assertThat(pullRequestTiming.getMergedAt()).isEqualTo(mergedAt),
                () -> assertThat(pullRequestTiming.getClosedAt()).isEqualTo(mergedAt)
        );
    }

    @Test
    void createMerged에서_생성_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when & then
        assertThatThrownBy(() -> PullRequestTiming.createMerged(null, mergedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 생성 시각은 필수입니다.");
    }

    @Test
    void createMerged에서_병합_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when & then
        assertThatThrownBy(() -> PullRequestTiming.createMerged(createdAt, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("병합 시각은 필수입니다.");
    }

    @Test
    void createMerged에서_생성_시각이_병합_시각보다_이후면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 14, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when & then
        assertThatThrownBy(() -> PullRequestTiming.createMerged(createdAt, mergedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("병합 시각은 생성 시각 이후여야 합니다.");
    }

    @Test
    void createClosed로_CLOSED_상태의_시간_정보를_생성한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 14, 0);

        // when
        PullRequestTiming pullRequestTiming = PullRequestTiming.createClosed(createdAt, closedAt);

        // then
        assertAll(
                () -> assertThat(pullRequestTiming.getPullRequestCreatedAt()).isEqualTo(createdAt),
                () -> assertThat(pullRequestTiming.getMergedAt()).isNull(),
                () -> assertThat(pullRequestTiming.getClosedAt()).isEqualTo(closedAt)
        );
    }

    @Test
    void createClosed에서_생성_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 14, 0);

        // when & then
        assertThatThrownBy(() -> PullRequestTiming.createClosed(null, closedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 생성 시각은 필수입니다.");
    }

    @Test
    void createClosed에서_종료_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when & then
        assertThatThrownBy(() -> PullRequestTiming.createClosed(createdAt, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시각은 필수입니다.");
    }

    @Test
    void createClosed에서_생성_시각이_종료_시각보다_이후면_예외가_발생한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 16, 0);
        LocalDateTime closedAt = LocalDateTime.of(2024, 1, 15, 14, 0);

        // when & then
        assertThatThrownBy(() -> PullRequestTiming.createClosed(createdAt, closedAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시각은 생성 시각 이후여야 합니다.");
    }

    @Test
    void calculateMergeTimeMinutes는_병합_시간을_분_단위로_계산한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 30);

        PullRequestTiming pullRequestTiming = PullRequestTiming.createMerged(createdAt, mergedAt);

        // when
        long mergeTimeMinutes = pullRequestTiming.calculateMergeTimeMinutes();

        // then
        assertThat(mergeTimeMinutes).isEqualTo(150);
    }

    @Test
    void calculateMergeTimeMinutes는_병합되지_않은_PullRequest면_예외가_발생한다() {
        // given
        PullRequestTiming pullRequestTiming = PullRequestTiming.createOpen(LocalDateTime.of(2024, 1, 15, 10, 0));

        // when & then
        assertThatThrownBy(() -> pullRequestTiming.calculateMergeTimeMinutes())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("병합되지 않은 PullRequest입니다.");
    }

    @Test
    void 동등성을_비교한다() {
        // given
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

        PullRequestTiming pullRequestTiming1 = PullRequestTiming.createMerged(createdAt, mergedAt);
        PullRequestTiming pullRequestTiming2 = PullRequestTiming.createMerged(createdAt, mergedAt);

        // then
        assertThat(pullRequestTiming1).isEqualTo(pullRequestTiming2);
    }
}
