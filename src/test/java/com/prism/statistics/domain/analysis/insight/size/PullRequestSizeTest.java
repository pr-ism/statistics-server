package com.prism.statistics.domain.analysis.insight.size;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.analysis.insight.size.vo.SizeScoreWeight;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestSizeTest {

    @Test
    void PR_크기를_생성한다() {
        // given
        Long pullRequestId = 1L;
        int additionCount = 100;
        int deletionCount = 50;
        int changedFileCount = 5;
        BigDecimal fileChangeDiversity = new BigDecimal("0.5000");

        // when
        PullRequestSize size = PullRequestSize.create(
                pullRequestId,
                additionCount,
                deletionCount,
                changedFileCount,
                fileChangeDiversity
        );

        // then
        assertAll(
                () -> assertThat(size.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(size.getAdditionCount()).isEqualTo(additionCount),
                () -> assertThat(size.getDeletionCount()).isEqualTo(deletionCount),
                () -> assertThat(size.getChangedFileCount()).isEqualTo(changedFileCount),
                () -> assertThat(size.getFileChangeDiversity()).isEqualTo(fileChangeDiversity),
                () -> assertThat(size.getSizeScore()).isEqualByComparingTo(new BigDecimal("155")),
                () -> assertThat(size.getSizeGrade()).isEqualTo(SizeGrade.M)
        );
    }

    @Test
    void 커스텀_가중치로_PR_크기를_생성한다() {
        // given
        Long pullRequestId = 1L;
        SizeScoreWeight customWeight = SizeScoreWeight.of(
                new BigDecimal("2.0"),
                new BigDecimal("1.0"),
                new BigDecimal("10.0")
        );

        // when
        PullRequestSize size = PullRequestSize.createWithWeight(
                pullRequestId,
                100,
                50,
                5,
                new BigDecimal("0.5000"),
                customWeight
        );

        // then
        assertAll(
                () -> assertThat(size.getSizeScore()).isEqualByComparingTo(new BigDecimal("300")),
                () -> assertThat(size.getSizeGrade()).isEqualTo(SizeGrade.L)
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestSize.create(null, 100, 50, 5, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pull Request ID는 필수입니다.");
    }

    @Test
    void 추가_라인_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestSize.create(1L, -1, 50, 5, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 삭제_라인_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestSize.create(1L, 100, -1, 5, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 변경_파일_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestSize.create(1L, 100, 50, -1, BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 파일 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 파일_변경_다양도가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestSize.create(1L, 100, 50, 5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 변경 다양도는 필수입니다.");
    }

    @Test
    void 가중치가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestSize.createWithWeight(
                1L, 100, 50, 5, new BigDecimal("0.5"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가중치는 필수입니다.");
    }

    @Test
    void 파일_변경_다양도가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestSize.create(1L, 100, 50, 5, new BigDecimal("-0.1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 변경 다양도는 0보다 작을 수 없습니다.");
    }

    @Test
    void 파일_변경_다양도가_1을_초과하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestSize.create(1L, 100, 50, 5, new BigDecimal("1.1")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 변경 다양도는 1을 초과할 수 없습니다.");
    }

    @Test
    void 파일_변경_다양도를_계산한다() {
        // given
        int added = 2;
        int modified = 2;
        int deleted = 2;
        int renamed = 2;

        // when
        BigDecimal diversity = PullRequestSize.calculateFileChangeDiversity(added, modified, deleted, renamed);

        // then
        assertThat(diversity).isEqualByComparingTo(new BigDecimal("0.75"));
    }

    @Test
    void 단일_타입만_있으면_다양도가_0이다() {
        // when
        BigDecimal diversity = PullRequestSize.calculateFileChangeDiversity(10, 0, 0, 0);

        // then
        assertThat(diversity).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void 파일이_없으면_다양도가_0이다() {
        // when
        BigDecimal diversity = PullRequestSize.calculateFileChangeDiversity(0, 0, 0, 0);

        // then
        assertThat(diversity).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void 커스텀_가중치로_재계산한다() {
        // given
        PullRequestSize original = PullRequestSize.create(1L, 100, 50, 5, new BigDecimal("0.5"));
        SizeScoreWeight newWeight = SizeScoreWeight.of(
                new BigDecimal("2.0"),
                new BigDecimal("2.0"),
                new BigDecimal("20.0")
        );

        // when
        PullRequestSize recalculated = original.recalculateWithWeight(newWeight);

        // then
        assertThat(recalculated.getSizeScore()).isEqualByComparingTo(new BigDecimal("400"));
    }

    @Test
    void 총_변경량을_계산한다() {
        // given
        PullRequestSize size = PullRequestSize.create(1L, 100, 50, 5, BigDecimal.ZERO);

        // when
        int totalChanges = size.getTotalChanges();

        // then
        assertThat(totalChanges).isEqualTo(150);
    }

    @Test
    void Large_이상_여부를_확인한다() {
        // given
        PullRequestSize smallSize = PullRequestSize.create(1L, 50, 30, 3, BigDecimal.ZERO);
        PullRequestSize largeSize = PullRequestSize.create(1L, 500, 300, 10, BigDecimal.ZERO);

        // then
        assertAll(
                () -> assertThat(smallSize.isLargeOrAbove()).isFalse(),
                () -> assertThat(largeSize.isLargeOrAbove()).isTrue()
        );
    }

    @Test
    void 높은_다양도_여부를_확인한다() {
        // given
        PullRequestSize lowDiversity = PullRequestSize.create(1L, 100, 50, 5, new BigDecimal("0.3"));
        PullRequestSize highDiversity = PullRequestSize.create(1L, 100, 50, 5, new BigDecimal("0.6"));

        // then
        assertAll(
                () -> assertThat(lowDiversity.hasHighDiversity()).isFalse(),
                () -> assertThat(highDiversity.hasHighDiversity()).isTrue()
        );
    }
}
