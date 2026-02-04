package com.prism.statistics.domain.analysis.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestOpenedCommitDensityTest {

    @Test
    void pull_request_오픈_커밋_밀도를_생성한다() {
        // given
        Long pullRequestId = 1L;
        BigDecimal commitDensityPerFile = new BigDecimal("1.5000");
        BigDecimal commitDensityPerChange = new BigDecimal("0.200000");

        // when
        PullRequestOpenedCommitDensity density = PullRequestOpenedCommitDensity.create(
                pullRequestId,
                commitDensityPerFile,
                commitDensityPerChange
        );

        // then
        assertAll(
                () -> assertThat(density.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(density.getCommitDensityPerFile()).isEqualTo(commitDensityPerFile),
                () -> assertThat(density.getCommitDensityPerChange()).isEqualTo(commitDensityPerChange)
        );
    }

    @Test
    void pull_request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedCommitDensity.create(null, BigDecimal.ONE, BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("PR ID는 필수입니다.")
        );
    }

    @Test
    void 파일_기준_커밋_밀도가_null이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedCommitDensity.create(1L, null, BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일 기준 커밋 밀도는 필수입니다.")
        );
    }

    @Test
    void 파일_기준_커밋_밀도가_음수이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedCommitDensity.create(1L, new BigDecimal("-0.01"), BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일 기준 커밋 밀도는 0보다 작을 수 없습니다.")
        );
    }

    @Test
    void 변경량_기준_커밋_밀도가_null이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedCommitDensity.create(1L, BigDecimal.ONE, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("변경량 기준 커밋 밀도는 필수입니다.")
        );
    }

    @Test
    void 변경량_기준_커밋_밀도가_음수이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedCommitDensity.create(1L, BigDecimal.ONE, new BigDecimal("-0.01")))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("변경량 기준 커밋 밀도는 0보다 작을 수 없습니다.")
        );
    }
}
