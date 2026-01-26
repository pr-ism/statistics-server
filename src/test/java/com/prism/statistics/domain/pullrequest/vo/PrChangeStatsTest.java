package com.prism.statistics.domain.pullrequest.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrChangeStatsTest {

    @Test
    void 변경_통계를_생성한다() {
        // when
        PrChangeStats stats = PrChangeStats.create(10, 100, 50);

        // then
        assertThat(stats.getChangedFileCount()).isEqualTo(10);
        assertThat(stats.getAdditionCount()).isEqualTo(100);
        assertThat(stats.getDeletionCount()).isEqualTo(50);
    }

    @Test
    void 변경_파일_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrChangeStats.create(-1, 100, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 파일 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 추가_라인_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrChangeStats.create(10, -1, 50))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("추가 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 삭제_라인_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrChangeStats.create(10, 100, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("삭제 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 변경_파일이_없는데_추가_라인이_있으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrChangeStats.create(0, 100, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경된 파일이 없으면 추가/삭제 라인이 있을 수 없습니다.");
    }

    @Test
    void 변경_파일이_없는데_삭제_라인이_있으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrChangeStats.create(0, 0, 100))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경된 파일이 없으면 추가/삭제 라인이 있을 수 없습니다.");
    }

    @Test
    void 변경_파일과_라인이_모두_0이면_정상_생성된다() {
        // when
        PrChangeStats stats = PrChangeStats.create(0, 0, 0);

        // then
        assertThat(stats.getChangedFileCount()).isZero();
        assertThat(stats.getAdditionCount()).isZero();
        assertThat(stats.getDeletionCount()).isZero();
    }

    @Test
    void 총_변경_라인_수를_계산한다() {
        // given
        PrChangeStats stats = PrChangeStats.create(5, 200, 100);

        // when
        long totalChanges = stats.getTotalChanges();

        // then
        assertThat(totalChanges).isEqualTo(300);
    }

    @Test
    void 동등성을_비교한다() {
        // given
        PrChangeStats stats1 = PrChangeStats.create(10, 100, 50);
        PrChangeStats stats2 = PrChangeStats.create(10, 100, 50);

        // then
        assertThat(stats1).isEqualTo(stats2);
    }

    @Test
    void 값이_다르면_동등하지_않다() {
        // given
        PrChangeStats stats1 = PrChangeStats.create(10, 100, 50);
        PrChangeStats stats2 = PrChangeStats.create(10, 100, 51);

        // then
        assertThat(stats1).isNotEqualTo(stats2);
    }
}
