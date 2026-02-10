package com.prism.statistics.domain.analysis.insight.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SizeScoreWeightsTest {

    @Test
    void 기본_가중치는_모두_1이다() {
        // given
        SizeScoreWeights weights = SizeScoreWeights.DEFAULT;

        // then
        assertThat(weights.getAdditionWeight()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(weights.getDeletionWeight()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(weights.getChangedFileWeight()).isEqualByComparingTo(BigDecimal.ONE);
    }

    @Test
    void 기본_가중치로_점수를_계산한다() {
        // given
        SizeScoreWeights weights = SizeScoreWeights.DEFAULT;
        int additions = 100;
        int deletions = 50;
        int changedFileCount = 10;

        // when
        BigDecimal score = weights.calculateScore(additions, deletions, changedFileCount);

        // then
        // 100*1 + 50*1 + 10*1 = 160
        assertThat(score).isEqualByComparingTo(new BigDecimal("160.00"));
    }

    @Test
    void 커스텀_가중치를_생성한다() {
        // given
        BigDecimal additionWeight = new BigDecimal("1.5");
        BigDecimal deletionWeight = new BigDecimal("0.5");
        BigDecimal changedFileWeight = new BigDecimal("2.0");

        // when
        SizeScoreWeights weights = SizeScoreWeights.create(additionWeight, deletionWeight, changedFileWeight);

        // then
        assertThat(weights.getAdditionWeight()).isEqualByComparingTo(additionWeight);
        assertThat(weights.getDeletionWeight()).isEqualByComparingTo(deletionWeight);
        assertThat(weights.getChangedFileWeight()).isEqualByComparingTo(changedFileWeight);
    }

    @Test
    void 커스텀_가중치로_점수를_계산한다() {
        // given
        SizeScoreWeights weights = SizeScoreWeights.create(
                new BigDecimal("1.5"),
                new BigDecimal("0.5"),
                new BigDecimal("2.0")
        );
        int additions = 100;
        int deletions = 50;
        int changedFileCount = 10;

        // when
        BigDecimal score = weights.calculateScore(additions, deletions, changedFileCount);

        // then
        // 100*1.5 + 50*0.5 + 10*2.0 = 150 + 25 + 20 = 195
        assertThat(score).isEqualByComparingTo(new BigDecimal("195.00"));
    }

    @Test
    void addition_가중치가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeights.create(null, BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("addition 가중치는 필수입니다.");
    }

    @Test
    void deletion_가중치가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeights.create(BigDecimal.ONE, null, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("deletion 가중치는 필수입니다.");
    }

    @Test
    void changedFile_가중치가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeights.create(BigDecimal.ONE, BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("changedFile 가중치는 필수입니다.");
    }

    @Test
    void addition_가중치가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeights.create(new BigDecimal("-1"), BigDecimal.ONE, BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("addition 가중치는 0보다 작을 수 없습니다.");
    }

    @Test
    void deletion_가중치가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeights.create(BigDecimal.ONE, new BigDecimal("-1"), BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("deletion 가중치는 0보다 작을 수 없습니다.");
    }

    @Test
    void changedFile_가중치가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeScoreWeights.create(BigDecimal.ONE, BigDecimal.ONE, new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("changedFile 가중치는 0보다 작을 수 없습니다.");
    }

    @Test
    void 가중치가_0이어도_정상_생성된다() {
        // when
        SizeScoreWeights weights = SizeScoreWeights.create(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        // then
        assertThat(weights.getAdditionWeight()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(weights.getDeletionWeight()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(weights.getChangedFileWeight()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void 모든_값이_0이면_점수도_0이다() {
        // given
        SizeScoreWeights weights = SizeScoreWeights.DEFAULT;

        // when
        BigDecimal score = weights.calculateScore(0, 0, 0);

        // then
        assertThat(score).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
