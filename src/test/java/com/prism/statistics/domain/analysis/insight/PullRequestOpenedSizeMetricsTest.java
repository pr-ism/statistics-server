package com.prism.statistics.domain.analysis.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.domain.analysis.insight.enums.PullRequestSizeGrade;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestOpenedSizeMetricsTest {

    @Test
    void PR_크기_메트릭을_생성한다() {
        // given
        Long pullRequestId = 1L;
        BigDecimal sizeScore = new BigDecimal("150.00");
        PullRequestSizeGrade sizeGrade = PullRequestSizeGrade.M;
        int changedFileCount = 5;
        int addedFileCount = 2;
        int modifiedFileCount = 2;
        int removedFileCount = 1;
        int renamedFileCount = 0;

        // when
        PullRequestOpenedSizeMetrics metrics = PullRequestOpenedSizeMetrics.create(
                pullRequestId,
                sizeScore,
                sizeGrade,
                changedFileCount,
                addedFileCount,
                modifiedFileCount,
                removedFileCount,
                renamedFileCount
        );

        // then
        assertAll(
                () -> assertThat(metrics.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(metrics.getSizeScore()).isEqualByComparingTo(sizeScore),
                () -> assertThat(metrics.getSizeGrade()).isEqualTo(sizeGrade),
                () -> assertThat(metrics.getChangedFileCount()).isEqualTo(changedFileCount),
                () -> assertThat(metrics.getAddedFileCount()).isEqualTo(addedFileCount),
                () -> assertThat(metrics.getModifiedFileCount()).isEqualTo(modifiedFileCount),
                () -> assertThat(metrics.getRemovedFileCount()).isEqualTo(removedFileCount),
                () -> assertThat(metrics.getRenamedFileCount()).isEqualTo(renamedFileCount)
        );
    }

    @Test
    void pull_request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                null,
                BigDecimal.TEN,
                PullRequestSizeGrade.S,
                1, 1, 0, 0, 0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pull Request ID는 필수입니다.");
    }

    @Test
    void 크기_점수가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                1L,
                null,
                PullRequestSizeGrade.S,
                1, 1, 0, 0, 0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("크기 점수는 필수입니다.");
    }

    @Test
    void 크기_점수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                1L,
                new BigDecimal("-1"),
                PullRequestSizeGrade.S,
                1, 1, 0, 0, 0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("크기 점수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 크기_등급이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                1L,
                BigDecimal.TEN,
                null,
                1, 1, 0, 0, 0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("크기 등급은 필수입니다.");
    }

    @Test
    void 변경_파일_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                1L,
                BigDecimal.TEN,
                PullRequestSizeGrade.S,
                -1, 0, 0, 0, 0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 파일 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 추가_파일_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                1L,
                BigDecimal.TEN,
                PullRequestSizeGrade.S,
                1, -1, 0, 0, 0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가 파일 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 수정_파일_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                1L,
                BigDecimal.TEN,
                PullRequestSizeGrade.S,
                1, 0, -1, 0, 0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수정 파일 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 삭제_파일_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                1L,
                BigDecimal.TEN,
                PullRequestSizeGrade.S,
                1, 0, 0, -1, 0
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 파일 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 이름변경_파일_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestOpenedSizeMetrics.create(
                1L,
                BigDecimal.TEN,
                PullRequestSizeGrade.S,
                1, 0, 0, 0, -1
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름 변경 파일 수는 0보다 작을 수 없습니다.");
    }
}
