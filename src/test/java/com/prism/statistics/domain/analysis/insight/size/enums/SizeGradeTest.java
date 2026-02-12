package com.prism.statistics.domain.analysis.insight.size.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SizeGradeTest {

    @ParameterizedTest
    @CsvSource({
            "0, XS",
            "9, XS",
            "10, S",
            "99, S",
            "100, M",
            "299, M",
            "300, L",
            "999, L",
            "1000, XL",
            "5000, XL"
    })
    void 점수에_따라_등급을_반환한다(int score, SizeGrade expected) {
        // when
        SizeGrade grade = SizeGrade.fromScore(score);

        // then
        assertThat(grade).isEqualTo(expected);
    }

    @Test
    void BigDecimal_점수로_등급을_계산한다() {
        // given
        BigDecimal score = new BigDecimal("150.5");

        // when
        SizeGrade grade = SizeGrade.fromScore(score);

        // then
        assertThat(grade).isEqualTo(SizeGrade.M);
    }

    @Test
    void 점수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGrade.fromScore(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("점수는 0보다 작을 수 없습니다.");
    }

    @Test
    void BigDecimal_점수가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGrade.fromScore((BigDecimal) null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("점수는 필수입니다.");
    }

    @Test
    void 커스텀_임계값으로_등급을_계산한다() {
        // given
        int[] customThresholds = {5, 50, 200, 500};

        // then
        assertAll(
                () -> assertThat(SizeGrade.fromScoreWithThresholds(4, customThresholds)).isEqualTo(SizeGrade.XS),
                () -> assertThat(SizeGrade.fromScoreWithThresholds(49, customThresholds)).isEqualTo(SizeGrade.S),
                () -> assertThat(SizeGrade.fromScoreWithThresholds(199, customThresholds)).isEqualTo(SizeGrade.M),
                () -> assertThat(SizeGrade.fromScoreWithThresholds(499, customThresholds)).isEqualTo(SizeGrade.L),
                () -> assertThat(SizeGrade.fromScoreWithThresholds(500, customThresholds)).isEqualTo(SizeGrade.XL),
                () -> assertThat(SizeGrade.fromScoreWithThresholds(5, customThresholds)).isEqualTo(SizeGrade.S),
                () -> assertThat(SizeGrade.fromScoreWithThresholds(50, customThresholds)).isEqualTo(SizeGrade.M),
                () -> assertThat(SizeGrade.fromScoreWithThresholds(200, customThresholds)).isEqualTo(SizeGrade.L)
        );
    }

    @Test
    void 임계값_배열이_4개가_아니면_예외가_발생한다() {
        // given
        int[] invalidThresholds = {10, 100, 300};

        // when & then
        assertThatThrownBy(() -> SizeGrade.fromScoreWithThresholds(50, invalidThresholds))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("임계값 배열은 4개 요소가 필요합니다.");
    }

    @Test
    void 임계값_배열이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGrade.fromScoreWithThresholds(50, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("임계값 배열은 4개 요소가 필요합니다.");
    }

    @Test
    void 등급별_속성을_확인한다() {
        // then
        assertAll(
                () -> assertThat(SizeGrade.XS.getDescription()).isEqualTo("Extra Small"),
                () -> assertThat(SizeGrade.XS.getMinScore()).isZero(),
                () -> assertThat(SizeGrade.XS.getMaxScore()).isEqualTo(10),
                () -> assertThat(SizeGrade.S.getDescription()).isEqualTo("Small"),
                () -> assertThat(SizeGrade.S.getMinScore()).isEqualTo(10),
                () -> assertThat(SizeGrade.S.getMaxScore()).isEqualTo(100),
                () -> assertThat(SizeGrade.M.getDescription()).isEqualTo("Medium"),
                () -> assertThat(SizeGrade.M.getMinScore()).isEqualTo(100),
                () -> assertThat(SizeGrade.M.getMaxScore()).isEqualTo(300),
                () -> assertThat(SizeGrade.L.getDescription()).isEqualTo("Large"),
                () -> assertThat(SizeGrade.L.getMinScore()).isEqualTo(300),
                () -> assertThat(SizeGrade.L.getMaxScore()).isEqualTo(1000),
                () -> assertThat(SizeGrade.XL.getDescription()).isEqualTo("Extra Large"),
                () -> assertThat(SizeGrade.XL.getMinScore()).isEqualTo(1000),
                () -> assertThat(SizeGrade.XL.getMaxScore()).isEqualTo(Integer.MAX_VALUE)
        );
    }
}
