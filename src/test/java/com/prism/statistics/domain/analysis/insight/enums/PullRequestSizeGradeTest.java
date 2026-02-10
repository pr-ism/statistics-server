package com.prism.statistics.domain.analysis.insight.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestSizeGradeTest {

    @ParameterizedTest
    @CsvSource({
        "0, XS",
        "5, XS",
        "9, XS",
        "10, S",
        "50, S",
        "99, S",
        "100, M",
        "200, M",
        "299, M",
        "300, L",
        "500, L",
        "999, L",
        "1000, XL",
        "5000, XL",
        "10000, XL"
    })
    void 변경량에_따라_올바른_등급을_분류한다(int totalChanges, PullRequestSizeGrade expected) {
        // when
        PullRequestSizeGrade result = PullRequestSizeGrade.classify(totalChanges);

        // then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    void 커스텀_임계값으로_등급을_분류한다() {
        // given
        int xsThreshold = 5;
        int sThreshold = 20;
        int mThreshold = 50;
        int lThreshold = 100;

        // when & then
        assertAll(
            () -> assertThat(
                PullRequestSizeGrade.classifyWithThresholds(3, xsThreshold, sThreshold, mThreshold,
                    lThreshold))
                .isEqualTo(PullRequestSizeGrade.XS),
            () -> assertThat(
                PullRequestSizeGrade.classifyWithThresholds(10, xsThreshold, sThreshold,
                    mThreshold, lThreshold))
                .isEqualTo(PullRequestSizeGrade.S),
            () -> assertThat(
                PullRequestSizeGrade.classifyWithThresholds(30, xsThreshold, sThreshold,
                    mThreshold, lThreshold))
                .isEqualTo(PullRequestSizeGrade.M),
            () -> assertThat(
                PullRequestSizeGrade.classifyWithThresholds(80, xsThreshold, sThreshold,
                    mThreshold, lThreshold))
                .isEqualTo(PullRequestSizeGrade.L),
            () -> assertThat(
                PullRequestSizeGrade.classifyWithThresholds(150, xsThreshold, sThreshold,
                    mThreshold, lThreshold))
                .isEqualTo(PullRequestSizeGrade.XL)
        );
    }

    @Test
    void 각_등급의_라벨을_반환한다() {
        assertAll(
            () -> assertThat(PullRequestSizeGrade.XS.getLabel()).isEqualTo("XS"),
            () -> assertThat(PullRequestSizeGrade.S.getLabel()).isEqualTo("S"),
            () -> assertThat(PullRequestSizeGrade.M.getLabel()).isEqualTo("M"),
            () -> assertThat(PullRequestSizeGrade.L.getLabel()).isEqualTo("L"),
            () -> assertThat(PullRequestSizeGrade.XL.getLabel()).isEqualTo("XL")
        );
    }

    @Test
    void 경계값에서_올바르게_분류된다() {
        assertAll(
            // XS와 S의 경계 (10)
            () -> assertThat(PullRequestSizeGrade.classify(9)).isEqualTo(PullRequestSizeGrade.XS),
            () -> assertThat(PullRequestSizeGrade.classify(10)).isEqualTo(PullRequestSizeGrade.S),

            // S와 M의 경계 (100)
            () -> assertThat(PullRequestSizeGrade.classify(99)).isEqualTo(PullRequestSizeGrade.S),
            () -> assertThat(PullRequestSizeGrade.classify(100)).isEqualTo(PullRequestSizeGrade.M),

            // M과 L의 경계 (300)
            () -> assertThat(PullRequestSizeGrade.classify(299)).isEqualTo(PullRequestSizeGrade.M),
            () -> assertThat(PullRequestSizeGrade.classify(300)).isEqualTo(PullRequestSizeGrade.L),

            // L과 XL의 경계 (1000)
            () -> assertThat(PullRequestSizeGrade.classify(999)).isEqualTo(PullRequestSizeGrade.L),
            () -> assertThat(PullRequestSizeGrade.classify(1000)).isEqualTo(PullRequestSizeGrade.XL)
        );
    }
}
