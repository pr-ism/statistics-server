package com.prism.statistics.domain.analysis.insight.lifecycle;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestLifecycleTest {

    @Test
    void PR_생명주기를_생성한다() {
        // given
        Long pullRequestId = 1L;
        LocalDateTime reviewReadyAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        DurationMinutes timeToMerge = DurationMinutes.of(120L);
        DurationMinutes totalLifespan = DurationMinutes.of(180L);
        DurationMinutes activeWork = DurationMinutes.of(100L);
        int stateChangeCount = 2;
        boolean reopened = false;
        boolean closedWithoutReview = false;

        // when
        PullRequestLifecycle lifecycle = PullRequestLifecycle.create(
                pullRequestId,
                reviewReadyAt,
                timeToMerge,
                totalLifespan,
                activeWork,
                stateChangeCount,
                reopened,
                closedWithoutReview
        );

        // then
        assertAll(
                () -> assertThat(lifecycle.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(lifecycle.getReviewReadyAt()).isEqualTo(reviewReadyAt),
                () -> assertThat(lifecycle.getTimeToMerge()).isEqualTo(timeToMerge),
                () -> assertThat(lifecycle.getTotalLifespan()).isEqualTo(totalLifespan),
                () -> assertThat(lifecycle.getActiveWork()).isEqualTo(activeWork),
                () -> assertThat(lifecycle.getStateChangeCount()).isEqualTo(stateChangeCount),
                () -> assertThat(lifecycle.isReopened()).isFalse(),
                () -> assertThat(lifecycle.isClosedWithoutReview()).isFalse()
        );
    }

    @Test
    void 진행_중인_PR_생명주기를_생성한다() {
        // given
        Long pullRequestId = 1L;
        LocalDateTime reviewReadyAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        DurationMinutes activeWork = DurationMinutes.of(60L);

        // when
        PullRequestLifecycle lifecycle = PullRequestLifecycle.createInProgress(
                pullRequestId,
                reviewReadyAt,
                activeWork,
                1,
                false
        );

        // then
        assertAll(
                () -> assertThat(lifecycle.getTimeToMerge()).isNull(),
                () -> assertThat(lifecycle.getTotalLifespan()).isNull(),
                () -> assertThat(lifecycle.isMerged()).isFalse(),
                () -> assertThat(lifecycle.isClosed()).isFalse()
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // given
        LocalDateTime reviewReadyAt = LocalDateTime.now();
        DurationMinutes activeWork = DurationMinutes.of(60L);

        // when & then
        assertThatThrownBy(() -> PullRequestLifecycle.create(
                null, reviewReadyAt, null, null, activeWork, 0, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pull Request ID는 필수입니다.");
    }

    @Test
    void 리뷰_가능_시점이_null이면_예외가_발생한다() {
        // given
        DurationMinutes activeWork = DurationMinutes.of(60L);

        // when & then
        assertThatThrownBy(() -> PullRequestLifecycle.create(
                1L, null, null, null, activeWork, 0, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 가능 시점은 필수입니다.");
    }

    @Test
    void 활성_작업_시간이_null이면_예외가_발생한다() {
        // given
        LocalDateTime reviewReadyAt = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> PullRequestLifecycle.create(
                1L, reviewReadyAt, null, null, null, 0, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활성 작업 시간은 필수입니다.");
    }

    @Test
    void 상태_변경_횟수가_음수이면_예외가_발생한다() {
        // given
        LocalDateTime reviewReadyAt = LocalDateTime.now();
        DurationMinutes activeWork = DurationMinutes.of(60L);

        // when & then
        assertThatThrownBy(() -> PullRequestLifecycle.create(
                1L, reviewReadyAt, null, null, activeWork, -1, false, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("상태 변경 횟수는 0보다 작을 수 없습니다.");
    }

    @Test
    void PR_종료_시_업데이트한다() {
        // given
        PullRequestLifecycle lifecycle = PullRequestLifecycle.createInProgress(
                1L,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                DurationMinutes.of(60L),
                1,
                false
        );
        DurationMinutes timeToMerge = DurationMinutes.of(120L);
        DurationMinutes totalLifespan = DurationMinutes.of(180L);
        DurationMinutes finalActiveWork = DurationMinutes.of(150L);

        // when
        lifecycle.updateOnClose(timeToMerge, totalLifespan, finalActiveWork, false);

        // then
        assertAll(
                () -> assertThat(lifecycle.getTimeToMerge()).isEqualTo(timeToMerge),
                () -> assertThat(lifecycle.getTotalLifespan()).isEqualTo(totalLifespan),
                () -> assertThat(lifecycle.getActiveWork()).isEqualTo(finalActiveWork),
                () -> assertThat(lifecycle.isMerged()).isTrue(),
                () -> assertThat(lifecycle.isClosed()).isTrue()
        );
    }

    @Test
    void 상태_변경_시_업데이트한다() {
        // given
        PullRequestLifecycle lifecycle = PullRequestLifecycle.createInProgress(
                1L,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                DurationMinutes.of(60L),
                1,
                false
        );
        DurationMinutes newActiveWork = DurationMinutes.of(90L);

        // when
        lifecycle.updateOnStateChange(newActiveWork, 2, true);

        // then
        assertAll(
                () -> assertThat(lifecycle.getActiveWork()).isEqualTo(newActiveWork),
                () -> assertThat(lifecycle.getStateChangeCount()).isEqualTo(2),
                () -> assertThat(lifecycle.isReopened()).isTrue()
        );
    }

    @Test
    void 상태_변경_여부를_확인한다() {
        // given
        PullRequestLifecycle lifecycleWithChanges = PullRequestLifecycle.createInProgress(
                1L, LocalDateTime.now(), DurationMinutes.of(60L), 2, false);
        PullRequestLifecycle lifecycleWithoutChanges = PullRequestLifecycle.createInProgress(
                2L, LocalDateTime.now(), DurationMinutes.of(60L), 0, false);

        // then
        assertAll(
                () -> assertThat(lifecycleWithChanges.hasStateChanges()).isTrue(),
                () -> assertThat(lifecycleWithoutChanges.hasStateChanges()).isFalse()
        );
    }
}
