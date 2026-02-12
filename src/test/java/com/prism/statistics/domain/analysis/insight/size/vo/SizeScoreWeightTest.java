package com.prism.statistics.domain.analysis.insight.size.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SizeScoreWeightTest {

    @Test
    void 가중치를_생성한다() {
        // given
        BigDecimal additionWeight = new BigDecimal("1.5");
        BigDecimal deletionWeight = new BigDecimal("0.5");
        BigDecimal fileWeight = new BigDecimal("10.0");

        // when
        SizeScoreWeight weight = SizeScoreWeight.of(additionWeight, deletionWeight, fileWeight);

        // then
        assertAll(
                () -> assertThat(weight.getAdditionWeight()).isEqualTo(additionWeight),
                () -> assertThat(weight.getDeletionWeight()).isEqualTo(deletionWeight),
                () -> assertThat(weight.getFileWeight()).isEqualTo(fileWeight)
        );
    }

    @Test
    void 기본_가중치를_생성한다() {
        // when
        SizeScoreWeight weight = SizeScoreWeight.defaultWeight();

        // then
        assertAll(
                () -> assertThat(weight.getAdditionWeight()).isEqualTo(BigDecimal.ONE),
                () -> assertThat(weight.getDeletionWeight()).isEqualTo(BigDecimal.ONE),
                () -> assertThat(weight.getFileWeight()).isEqualTo(BigDecimal.ONE)
        );
    }

    @Test
    void 추가_라인_가중치가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeight.of(null, BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가 라인 가중치는 필수입니다.");
    }

    @Test
    void 삭제_라인_가중치가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeight.of(BigDecimal.ONE, null, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 라인 가중치는 필수입니다.");
    }

    @Test
    void 파일_가중치가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeight.of(BigDecimal.ONE, BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 가중치는 필수입니다.");
    }

    @Test
    void 가중치가_음수이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> SizeScoreWeight.of(new BigDecimal("-1"), BigDecimal.ONE, BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("추가 라인 가중치는 0보다 작을 수 없습니다."),
                () -> assertThatThrownBy(() -> SizeScoreWeight.of(BigDecimal.ONE, new BigDecimal("-1"), BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("삭제 라인 가중치는 0보다 작을 수 없습니다."),
                () -> assertThatThrownBy(() -> SizeScoreWeight.of(BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("-1")))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일 가중치는 0보다 작을 수 없습니다.")
        );
    }

    @Test
    void 점수를_계산한다() {
        // given
        SizeScoreWeight weight = SizeScoreWeight.of(
                new BigDecimal("1.0"),
                new BigDecimal("0.5"),
                new BigDecimal("10.0")
        );
        int additions = 100;
        int deletions = 50;
        int fileCount = 5;

        // when
        BigDecimal score = weight.calculateScore(additions, deletions, fileCount);

        // then
        assertThat(score).isEqualByComparingTo(new BigDecimal("175.0"));
    }

    @Test
    void 기본_가중치로_점수를_계산한다() {
        // given
        SizeScoreWeight weight = SizeScoreWeight.defaultWeight();
        int additions = 100;
        int deletions = 50;
        int fileCount = 5;

        // when
        BigDecimal score = weight.calculateScore(additions, deletions, fileCount);

        // then
        assertThat(score).isEqualByComparingTo(new BigDecimal("155"));
    }

    @Test
    void 동등성을_비교한다() {
        // given
        SizeScoreWeight weight1 = SizeScoreWeight.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        SizeScoreWeight weight2 = SizeScoreWeight.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);

        // then
        assertThat(weight1).isEqualTo(weight2);
    }

    @Test
    void 값이_다르면_동등하지_않다() {
        // given
        SizeScoreWeight weight1 = SizeScoreWeight.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        SizeScoreWeight weight2 = SizeScoreWeight.of(BigDecimal.ONE, BigDecimal.ONE, BigDecimal.TEN);

        // then
        assertThat(weight1).isNotEqualTo(weight2);
    }
}
