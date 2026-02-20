package com.prism.statistics.domain.project.setting.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CoreTimeTest {

    @Test
    void 코어타임을_생성한다() {
        // given
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(18, 0);

        // when
        CoreTime coreTime = assertDoesNotThrow(
                () -> CoreTime.of(startTime, endTime)
        );

        // then
        assertAll(
                () -> assertThat(coreTime.getStartTime()).isEqualTo(startTime),
                () -> assertThat(coreTime.getEndTime()).isEqualTo(endTime)
        );
    }

    @Test
    void 기본_코어타임을_생성한다() {
        // when
        CoreTime coreTime = CoreTime.defaultCoreTime();

        // then
        assertAll(
                () -> assertThat(coreTime.getStartTime()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(coreTime.getEndTime()).isEqualTo(LocalTime.of(18, 0))
        );
    }

    @Test
    void 시작_시간이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CoreTime.of(null, LocalTime.of(18, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 시간은 필수입니다.");
    }

    @Test
    void 종료_시간이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CoreTime.of(LocalTime.of(10, 0), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("종료 시간은 필수입니다.");
    }

    @Test
    void 시작_시간이_종료_시간과_같으면_예외가_발생한다() {
        // given
        LocalTime sameTime = LocalTime.of(10, 0);

        // when & then
        assertThatThrownBy(() -> CoreTime.of(sameTime, sameTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 시간은 종료 시간보다 이전이어야 합니다.");
    }

    @Test
    void 시작_시간이_종료_시간보다_이후이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CoreTime.of(LocalTime.of(18, 0), LocalTime.of(10, 0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시작 시간은 종료 시간보다 이전이어야 합니다.");
    }

    @Test
    void 코어타임_내의_시간이면_true를_반환한다() {
        // given
        CoreTime coreTime = CoreTime.of(LocalTime.of(10, 0), LocalTime.of(18, 0));

        // when & then
        assertAll(
                () -> assertThat(coreTime.contains(LocalTime.of(10, 0))).isTrue(),
                () -> assertThat(coreTime.contains(LocalTime.of(14, 0))).isTrue(),
                () -> assertThat(coreTime.contains(LocalTime.of(18, 0))).isTrue()
        );
    }

    @Test
    void 코어타임_외의_시간이면_false를_반환한다() {
        // given
        CoreTime coreTime = CoreTime.of(LocalTime.of(10, 0), LocalTime.of(18, 0));

        // when & then
        assertAll(
                () -> assertThat(coreTime.contains(LocalTime.of(9, 59))).isFalse(),
                () -> assertThat(coreTime.contains(LocalTime.of(18, 1))).isFalse()
        );
    }

    @Test
    void contains에_null을_전달하면_예외가_발생한다() {
        // given
        CoreTime coreTime = CoreTime.defaultCoreTime();

        // when & then
        assertThatThrownBy(() -> coreTime.contains(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시간은 필수입니다.");
    }
}
