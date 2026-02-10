package com.prism.statistics.domain.analysis.insight.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prism.statistics.domain.analysis.insight.enums.PullRequestSizeGrade;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class SizeGradeThresholdsTest {

    @Test
    void 기본_임계값이_올바르게_설정된다() {
        // given
        SizeGradeThresholds thresholds = SizeGradeThresholds.DEFAULT;

        // then
        assertThat(thresholds.getXsThreshold()).isEqualTo(10);
        assertThat(thresholds.getSThreshold()).isEqualTo(100);
        assertThat(thresholds.getMThreshold()).isEqualTo(300);
        assertThat(thresholds.getLThreshold()).isEqualTo(1000);
    }

    @Test
    void 기본_임계값으로_등급을_분류한다() {
        // given
        SizeGradeThresholds thresholds = SizeGradeThresholds.DEFAULT;

        // when & then
        assertThat(thresholds.classify(5)).isEqualTo(PullRequestSizeGrade.XS);
        assertThat(thresholds.classify(50)).isEqualTo(PullRequestSizeGrade.S);
        assertThat(thresholds.classify(200)).isEqualTo(PullRequestSizeGrade.M);
        assertThat(thresholds.classify(500)).isEqualTo(PullRequestSizeGrade.L);
        assertThat(thresholds.classify(1500)).isEqualTo(PullRequestSizeGrade.XL);
    }

    @Test
    void 커스텀_임계값을_생성한다() {
        // when
        SizeGradeThresholds thresholds = SizeGradeThresholds.create(5, 50, 200, 500);

        // then
        assertThat(thresholds.getXsThreshold()).isEqualTo(5);
        assertThat(thresholds.getSThreshold()).isEqualTo(50);
        assertThat(thresholds.getMThreshold()).isEqualTo(200);
        assertThat(thresholds.getLThreshold()).isEqualTo(500);
    }

    @Test
    void 커스텀_임계값으로_등급을_분류한다() {
        // given
        SizeGradeThresholds thresholds = SizeGradeThresholds.create(5, 50, 200, 500);

        // when & then
        assertThat(thresholds.classify(3)).isEqualTo(PullRequestSizeGrade.XS);
        assertThat(thresholds.classify(25)).isEqualTo(PullRequestSizeGrade.S);
        assertThat(thresholds.classify(100)).isEqualTo(PullRequestSizeGrade.M);
        assertThat(thresholds.classify(300)).isEqualTo(PullRequestSizeGrade.L);
        assertThat(thresholds.classify(600)).isEqualTo(PullRequestSizeGrade.XL);
    }

    @Test
    void XS_임계값이_0이하이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGradeThresholds.create(0, 100, 300, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("XS 임계값은 0보다 커야 합니다.");

        assertThatThrownBy(() -> SizeGradeThresholds.create(-1, 100, 300, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("XS 임계값은 0보다 커야 합니다.");
    }

    @Test
    void XS_임계값이_S_임계값보다_크거나_같으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGradeThresholds.create(100, 100, 300, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("XS 임계값은 S 임계값보다 작아야 합니다.");

        assertThatThrownBy(() -> SizeGradeThresholds.create(150, 100, 300, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("XS 임계값은 S 임계값보다 작아야 합니다.");
    }

    @Test
    void S_임계값이_M_임계값보다_크거나_같으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGradeThresholds.create(10, 300, 300, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("S 임계값은 M 임계값보다 작아야 합니다.");

        assertThatThrownBy(() -> SizeGradeThresholds.create(10, 400, 300, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("S 임계값은 M 임계값보다 작아야 합니다.");
    }

    @Test
    void M_임계값이_L_임계값보다_크거나_같으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> SizeGradeThresholds.create(10, 100, 1000, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("M 임계값은 L 임계값보다 작아야 합니다.");

        assertThatThrownBy(() -> SizeGradeThresholds.create(10, 100, 1500, 1000))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("M 임계값은 L 임계값보다 작아야 합니다.");
    }

    @Test
    void 경계값에서_올바르게_분류된다() {
        // given
        SizeGradeThresholds thresholds = SizeGradeThresholds.DEFAULT;

        // XS와 S의 경계 (10)
        assertThat(thresholds.classify(9)).isEqualTo(PullRequestSizeGrade.XS);
        assertThat(thresholds.classify(10)).isEqualTo(PullRequestSizeGrade.S);

        // S와 M의 경계 (100)
        assertThat(thresholds.classify(99)).isEqualTo(PullRequestSizeGrade.S);
        assertThat(thresholds.classify(100)).isEqualTo(PullRequestSizeGrade.M);

        // M과 L의 경계 (300)
        assertThat(thresholds.classify(299)).isEqualTo(PullRequestSizeGrade.M);
        assertThat(thresholds.classify(300)).isEqualTo(PullRequestSizeGrade.L);

        // L과 XL의 경계 (1000)
        assertThat(thresholds.classify(999)).isEqualTo(PullRequestSizeGrade.L);
        assertThat(thresholds.classify(1000)).isEqualTo(PullRequestSizeGrade.XL);
    }
}
