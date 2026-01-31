package com.prism.statistics.domain.review.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewPeriodTest {

    @Test
    void empty로_빈_리뷰_기간을_생성한다() {
        // when
        ReviewPeriod reviewPeriod = ReviewPeriod.empty();

        // then
        assertAll(
                () -> assertThat(reviewPeriod.getFirstCommentedAt()).isNull(),
                () -> assertThat(reviewPeriod.getLastCommentedAt()).isNull()
        );
    }

    @Test
    void create로_리뷰_기간을_생성한다() {
        // given
        LocalDateTime first = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime last = LocalDateTime.of(2024, 1, 15, 12, 0);

        // when
        ReviewPeriod reviewPeriod = ReviewPeriod.create(first, last);

        // then
        assertAll(
                () -> assertThat(reviewPeriod.getFirstCommentedAt()).isEqualTo(first),
                () -> assertThat(reviewPeriod.getLastCommentedAt()).isEqualTo(last)
        );
    }

    @Test
    void 코멘트가_하나일_때_첫_번째와_마지막_시각이_동일하다() {
        // given
        LocalDateTime sameTime = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when
        ReviewPeriod reviewPeriod = ReviewPeriod.create(sameTime, sameTime);

        // then
        assertAll(
                () -> assertThat(reviewPeriod.getFirstCommentedAt()).isEqualTo(sameTime),
                () -> assertThat(reviewPeriod.getLastCommentedAt()).isEqualTo(sameTime)
        );
    }

    @Test
    void 첫_번째_코멘트_시각이_마지막_코멘트_시각보다_이후면_예외가_발생한다() {
        // given
        LocalDateTime first = LocalDateTime.of(2024, 1, 15, 14, 0);
        LocalDateTime last = LocalDateTime.of(2024, 1, 15, 10, 0);

        // when & then
        assertThatThrownBy(() -> ReviewPeriod.create(first, last))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("첫 번째 코멘트 시각은 마지막 코멘트 시각보다 이전이어야 합니다.");
    }

    @Test
    void 동등성을_비교한다() {
        // given
        LocalDateTime first = LocalDateTime.of(2024, 1, 15, 10, 0);
        LocalDateTime last = LocalDateTime.of(2024, 1, 15, 12, 0);

        ReviewPeriod reviewPeriod1 = ReviewPeriod.create(first, last);
        ReviewPeriod reviewPeriod2 = ReviewPeriod.create(first, last);

        // then
        assertThat(reviewPeriod1).isEqualTo(reviewPeriod2);
    }
}
