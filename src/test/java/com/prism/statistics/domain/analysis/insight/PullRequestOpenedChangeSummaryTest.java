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
class PullRequestOpenedChangeSummaryTest {

    @Test
    void pull_request_오픈_변경_요약을_생성한다() {
        // given
        Long pullRequestId = 1L;
        int totalChanges = 15;
        BigDecimal avgChangesPerFile = new BigDecimal("7.5000");

        // when
        PullRequestOpenedChangeSummary summary = PullRequestOpenedChangeSummary.create(
                pullRequestId,
                totalChanges,
                avgChangesPerFile
        );

        // then
        assertAll(
                () -> assertThat(summary.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(summary.getTotalChanges()).isEqualTo(totalChanges),
                () -> assertThat(summary.getAvgChangesPerFile()).isEqualTo(avgChangesPerFile)
        );
    }

    @Test
    void pull_request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedChangeSummary.create(null, 10, BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("Pull Request ID는 필수입니다.")
        );
    }

    @Test
    void 총_변경량이_음수이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedChangeSummary.create(1L, -1, BigDecimal.ONE))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("총 변경량은 0보다 작을 수 없습니다.")
        );
    }

    @Test
    void 파일당_평균_변경량이_null이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedChangeSummary.create(1L, 1, null))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일당 평균 변경량은 필수입니다.")
        );
    }

    @Test
    void 파일당_평균_변경량이_음수이면_예외가_발생한다() {
        // when & then
        assertAll(
                () -> assertThatThrownBy(() -> PullRequestOpenedChangeSummary.create(1L, 1, new BigDecimal("-0.01")))
                        .isInstanceOf(IllegalArgumentException.class)
                        .hasMessage("파일당 평균 변경량은 0보다 작을 수 없습니다.")
        );
    }
}
