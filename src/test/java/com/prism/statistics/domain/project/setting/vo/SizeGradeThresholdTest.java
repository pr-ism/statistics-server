package com.prism.statistics.domain.project.setting.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SizeGradeThresholdTest {

    @Test
    void 사이즈_등급_임계값을_생성한다() {
        // given & when
        SizeGradeThreshold threshold = assertDoesNotThrow(
                () -> SizeGradeThreshold.of(10, 100, 300, 1000)
        );

        // then
        assertAll(
                () -> assertThat(threshold.getSThreshold()).isEqualTo(10),
                () -> assertThat(threshold.getMThreshold()).isEqualTo(100),
                () -> assertThat(threshold.getLThreshold()).isEqualTo(300),
                () -> assertThat(threshold.getXlThreshold()).isEqualTo(1000)
        );
    }

    @Test
    void 기본_임계값을_생성한다() {
        // when
        SizeGradeThreshold threshold = SizeGradeThreshold.defaultThreshold();

        // then
        assertAll(
                () -> assertThat(threshold.getSThreshold()).isEqualTo(10),
                () -> assertThat(threshold.getMThreshold()).isEqualTo(100),
                () -> assertThat(threshold.getLThreshold()).isEqualTo(300),
                () -> assertThat(threshold.getXlThreshold()).isEqualTo(1000)
        );
    }

    @Test
    void 임계값이_0이하이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGradeThreshold.of(0, 100, 300, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("임계값은 0보다 커야 합니다.");
    }

    @Test
    void 임계값이_오름차순이_아니면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGradeThreshold.of(10, 300, 100, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("임계값은 S < M < L < XL 순서로 오름차순이어야 합니다.");
    }

    @Test
    void 같은_값이_있으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGradeThreshold.of(10, 100, 100, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("임계값은 S < M < L < XL 순서로 오름차순이어야 합니다.");
    }

    @Test
    void toArray로_배열을_반환한다() {
        // given
        SizeGradeThreshold threshold = SizeGradeThreshold.of(10, 100, 300, 1000);

        // when
        int[] array = threshold.toArray();

        // then
        assertThat(array).containsExactly(10, 100, 300, 1000);
    }
}
