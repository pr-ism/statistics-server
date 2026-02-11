package com.prism.statistics.domain.analysis.insight.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DurationMinutesTest {

    @Test
    void 분_단위_시간을_생성한다() {
        // when
        DurationMinutes duration = DurationMinutes.of(120);

        // then
        assertThat(duration.getMinutes()).isEqualTo(120);
    }

    @Test
    void 두_시각_사이의_분_단위_시간을_계산한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 12, 30);

        // when
        DurationMinutes duration = DurationMinutes.between(start, end);

        // then
        assertThat(duration.getMinutes()).isEqualTo(150);
    }

    @Test
    void 분이_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> DurationMinutes.of(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("소요 시간은 0보다 작을 수 없습니다.");
    }

    @Test
    void 시작_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime end = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> DurationMinutes.between(null, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 시각과 종료 시각은 필수입니다.");
    }

    @Test
    void 종료_시각이_시작_시각보다_이전이면_예외가_발생한다() {
        // given
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 12, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 10, 0);

        // when & then
        assertThatThrownBy(() -> DurationMinutes.between(start, end))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시각은 시작 시각 이후여야 합니다.");
    }

    @Test
    void 제로_시간을_생성한다() {
        // when
        DurationMinutes duration = DurationMinutes.zero();

        // then
        assertAll(
                () -> assertThat(duration.getMinutes()).isZero(),
                () -> assertThat(duration.isZero()).isTrue()
        );
    }

    @Test
    void 두_시간을_더한다() {
        // given
        DurationMinutes duration1 = DurationMinutes.of(60);
        DurationMinutes duration2 = DurationMinutes.of(30);

        // when
        DurationMinutes result = duration1.add(duration2);

        // then
        assertThat(result.getMinutes()).isEqualTo(90);
    }

    @Test
    void 두_시간을_뺀다() {
        // given
        DurationMinutes duration1 = DurationMinutes.of(60);
        DurationMinutes duration2 = DurationMinutes.of(30);

        // when
        DurationMinutes result = duration1.subtract(duration2);

        // then
        assertThat(result.getMinutes()).isEqualTo(30);
    }

    @Test
    void 뺄셈_결과가_음수이면_예외가_발생한다() {
        // given
        DurationMinutes duration1 = DurationMinutes.of(30);
        DurationMinutes duration2 = DurationMinutes.of(60);

        // when & then
        assertThatThrownBy(() -> duration1.subtract(duration2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결과 소요 시간은 0보다 작을 수 없습니다.");
    }

    @Test
    void 시간_단위로_변환한다() {
        // given
        DurationMinutes duration = DurationMinutes.of(150);

        // when
        long hours = duration.toHours();

        // then
        assertThat(hours).isEqualTo(2);
    }

    @Test
    void 일_단위로_변환한다() {
        // given
        DurationMinutes duration = DurationMinutes.of(2880); // 48시간

        // when
        long days = duration.toDays();

        // then
        assertThat(days).isEqualTo(2);
    }

    @Test
    void 크기를_비교한다() {
        // given
        DurationMinutes duration1 = DurationMinutes.of(60);
        DurationMinutes duration2 = DurationMinutes.of(30);

        // then
        assertAll(
                () -> assertThat(duration1.isGreaterThan(duration2)).isTrue(),
                () -> assertThat(duration2.isLessThan(duration1)).isTrue()
        );
    }

    @Test
    void 크기를_비교하는_다양한_상황을_검증한다() {
        // given
        DurationMinutes a = DurationMinutes.of(10);
        DurationMinutes b = DurationMinutes.of(5);
        DurationMinutes c = DurationMinutes.of(10);

        // when & then
        assertAll(
                () -> assertThat(a.isGreaterThan(b)).isTrue(),
                () -> assertThat(b.isGreaterThan(a)).isFalse(),
                () -> assertThat(a.isGreaterThan(c)).isFalse(),
                () -> assertThat(b.isLessThan(a)).isTrue(),
                () -> assertThat(a.isLessThan(b)).isFalse(),
                () -> assertThat(a.isLessThan(c)).isFalse()
        );
    }

    @Test
    void 한_시간이_다른_시간보다_큰지_비교한다() {
        // given
        DurationMinutes a = DurationMinutes.of(10);
        DurationMinutes b = DurationMinutes.of(5);

        // then
        assertAll(
                () -> assertThat(a.isGreaterThan(b)).isTrue(),
                () -> assertThat(b.isGreaterThan(a)).isFalse(),
                () -> assertThat(a.isGreaterThan(a)).isFalse()
        );
    }

    @Test
    void 한_시간이_다른_시간보다_작은지_비교한다() {
        // given
        DurationMinutes a = DurationMinutes.of(3);
        DurationMinutes b = DurationMinutes.of(8);

        // then
        assertAll(
                () -> assertThat(a.isLessThan(b)).isTrue(),
                () -> assertThat(b.isLessThan(a)).isFalse(),
                () -> assertThat(a.isLessThan(a)).isFalse()
        );
    }

    @Test
    void 동등성을_비교한다() {
        // given
        DurationMinutes duration1 = DurationMinutes.of(60);
        DurationMinutes duration2 = DurationMinutes.of(60);

        // then
        assertThat(duration1).isEqualTo(duration2);
    }

    @Test
    void 종료_시각이_null이면_예외가_발생한다() {
        // given
        LocalDateTime start = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> DurationMinutes.between(start, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 시각과 종료 시각은 필수입니다.");
    }

    @Test
    void null을_더하면_예외가_발생한다() {
        // given
        DurationMinutes duration = DurationMinutes.of(60);

        // when & then
        assertThatThrownBy(() -> duration.add(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("더할 대상은 null일 수 없습니다.");
    }

    @Test
    void null을_빼면_예외가_발생한다() {
        // given
        DurationMinutes duration = DurationMinutes.of(60);

        // when & then
        assertThatThrownBy(() -> duration.subtract(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("뺄 대상은 null일 수 없습니다.");
    }

    @Test
    void Zero가_아니면_isZero는_false를_반환한다() {
        // given
        DurationMinutes duration = DurationMinutes.of(1);

        // then
        assertThat(duration.isZero()).isFalse();
    }

    @Test
    void 시작과_종료_시각이_같으면_0분이다() {
        // given
        LocalDateTime time = LocalDateTime.of(2024, 1, 1, 10, 0);

        // when
        DurationMinutes duration = DurationMinutes.between(time, time);

        // then
        assertThat(duration.isZero()).isTrue();
    }

    @Test
    void isGreaterThan의_비교_대상이_null이면_예외가_발생한다() {
        // given
        DurationMinutes duration = DurationMinutes.of(10);

        // when & then
        assertThatThrownBy(() -> duration.isGreaterThan(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("비교 대상은 null일 수 없습니다.");
    }

    @Test
    void isLessThan의_비교_대상이_null이면_예외가_발생한다() {
        // given
        DurationMinutes duration = DurationMinutes.of(10);

        // when & then
        assertThatThrownBy(() -> duration.isLessThan(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("비교 대상은 null일 수 없습니다.");
    }
}
