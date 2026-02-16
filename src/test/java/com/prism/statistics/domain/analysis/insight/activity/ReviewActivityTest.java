package com.prism.statistics.domain.analysis.insight.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewActivityTest {

    @Test
    void 리뷰_활동량을_생성한다() {
        // given
        Long pullRequestId = 1L;
        int reviewRoundTrips = 3;
        int totalCommentCount = 15;
        int totalAdditions = 100;
        int totalDeletions = 50;
        int codeAdditionsAfterReview = 20;
        int codeDeletionsAfterReview = 10;
        int additionalReviewerCount = 1;

        // when
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(pullRequestId)
                .reviewRoundTrips(reviewRoundTrips)
                .totalCommentCount(totalCommentCount)
                .totalAdditions(totalAdditions)
                .totalDeletions(totalDeletions)
                .codeAdditionsAfterReview(codeAdditionsAfterReview)
                .codeDeletionsAfterReview(codeDeletionsAfterReview)
                .additionalReviewerCount(additionalReviewerCount)
                .build();

        // then
        assertAll(
                () -> assertThat(activity.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(activity.getReviewRoundTrips()).isEqualTo(reviewRoundTrips),
                () -> assertThat(activity.getTotalCommentCount()).isEqualTo(totalCommentCount),
                () -> assertThat(activity.getTotalAdditions()).isEqualTo(totalAdditions),
                () -> assertThat(activity.getTotalDeletions()).isEqualTo(totalDeletions),
                () -> assertThat(activity.getCodeAdditionsAfterReview()).isEqualTo(codeAdditionsAfterReview),
                () -> assertThat(activity.getCodeDeletionsAfterReview()).isEqualTo(codeDeletionsAfterReview),
                () -> assertThat(activity.isHasAdditionalReviewers()).isTrue(),
                () -> assertThat(activity.getAdditionalReviewerCount()).isEqualTo(additionalReviewerCount)
        );
    }

    @Test
    void 코멘트_밀도를_계산한다() {
        // given
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(3)
                .totalCommentCount(15)
                .totalAdditions(100)
                .totalDeletions(50)
                .build();

        // then
        assertThat(activity.getCommentDensity()).isEqualByComparingTo(new BigDecimal("0.1"));
    }

    @Test
    void 변경이_없으면_코멘트_밀도가_0이다() {
        // given
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(1)
                .totalCommentCount(5)
                .build();

        // then
        assertThat(activity.getCommentDensity()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void 리뷰가_없는_PR을_생성한다() {
        // when
        ReviewActivity activity = ReviewActivity.createWithoutReview(1L, 100, 50);

        // then
        assertAll(
                () -> assertThat(activity.getReviewRoundTrips()).isZero(),
                () -> assertThat(activity.getTotalCommentCount()).isZero(),
                () -> assertThat(activity.getCommentDensity()).isEqualByComparingTo(BigDecimal.ZERO),
                () -> assertThat(activity.hasReviewActivity()).isFalse()
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewActivity.builder()
                .pullRequestId(null)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pull Request ID는 필수입니다.");
    }

    @Test
    void 리뷰_왕복_횟수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(-1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 왕복 횟수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 새로운_리뷰_등록_시_업데이트한다() {
        // given
        ReviewActivity activity = ReviewActivity.createWithoutReview(1L, 100, 50);

        // when
        activity.updateOnNewReview(5);

        // then
        assertAll(
                () -> assertThat(activity.getReviewRoundTrips()).isEqualTo(1),
                () -> assertThat(activity.getTotalCommentCount()).isEqualTo(5),
                () -> assertThat(activity.getCommentDensity()).isEqualByComparingTo(new BigDecimal("0.033333")),
                () -> assertThat(activity.hasReviewActivity()).isTrue()
        );
    }

    @Test
    void 새로운_리뷰_등록_시_코멘트_수가_음수이면_예외가_발생한다() {
        // given
        ReviewActivity activity = ReviewActivity.createWithoutReview(1L, 100, 50);

        // when & then
        assertThatThrownBy(() -> activity.updateOnNewReview(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("신규 코멘트 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 리뷰_이후_코드_변경_시_추가_라인이_음수이면_예외가_발생한다() {
        // given
        ReviewActivity activity = ReviewActivity.createWithoutReview(1L, 100, 50);

        // when & then
        assertThatThrownBy(() -> activity.updateCodeChangesAfterReview(-1, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 이후 추가 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 리뷰_이후_코드_변경_시_삭제_라인이_음수이면_예외가_발생한다() {
        // given
        ReviewActivity activity = ReviewActivity.createWithoutReview(1L, 100, 50);

        // when & then
        assertThatThrownBy(() -> activity.updateCodeChangesAfterReview(10, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 이후 삭제 라인 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 리뷰_이후_코드_변경_시_업데이트한다() {
        // given
        ReviewActivity activity = ReviewActivity.createWithoutReview(1L, 100, 50);

        // when
        activity.updateCodeChangesAfterReview(30, 15);

        // then
        assertAll(
                () -> assertThat(activity.getCodeAdditionsAfterReview()).isEqualTo(30),
                () -> assertThat(activity.getCodeDeletionsAfterReview()).isEqualTo(15),
                () -> assertThat(activity.getTotalCodeChangesAfterReview()).isEqualTo(45)
        );
    }

    @Test
    void 리뷰어_추가_시_업데이트한다() {
        // given
        ReviewActivity activity = ReviewActivity.createWithoutReview(1L, 100, 50);

        // when
        activity.updateOnReviewerAdded();

        // then
        assertAll(
                () -> assertThat(activity.isHasAdditionalReviewers()).isTrue(),
                () -> assertThat(activity.getAdditionalReviewerCount()).isEqualTo(1)
        );
    }

    @Test
    void PR_변경량_업데이트_시_밀도를_재계산한다() {
        // given
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(1)
                .totalCommentCount(10)
                .totalAdditions(100)
                .totalDeletions(50)
                .build();

        // when
        activity.updateTotalChanges(200, 100);

        // then
        assertThat(activity.getCommentDensity()).isEqualByComparingTo(new BigDecimal("0.033333"));
    }

    @Test
    void 총_변경량을_계산한다() {
        // given
        ReviewActivity activity = ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(1)
                .totalCommentCount(5)
                .totalAdditions(100)
                .totalDeletions(50)
                .build();

        // when
        int totalChanges = activity.getTotalChanges();

        // then
        assertThat(totalChanges).isEqualTo(150);
    }

    @Test
    void 높은_코멘트_밀도_여부를_확인한다() {
        // given
        ReviewActivity highDensity = ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(1)
                .totalCommentCount(15)
                .totalAdditions(100)
                .totalDeletions(50)
                .build();
        ReviewActivity lowDensity = ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(1)
                .totalCommentCount(5)
                .totalAdditions(100)
                .totalDeletions(50)
                .build();

        // then
        assertAll(
                () -> assertThat(highDensity.hasHighCommentDensity()).isTrue(),
                () -> assertThat(lowDensity.hasHighCommentDensity()).isFalse()
        );
    }

    @Test
    void 유의미한_변경_여부를_확인한다() {
        // given
        ReviewActivity significant = ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(1)
                .totalCommentCount(5)
                .totalAdditions(100)
                .totalDeletions(50)
                .codeAdditionsAfterReview(10)
                .codeDeletionsAfterReview(5)
                .build();
        ReviewActivity insignificant = ReviewActivity.builder()
                .pullRequestId(1L)
                .reviewRoundTrips(1)
                .totalCommentCount(5)
                .totalAdditions(100)
                .totalDeletions(50)
                .codeAdditionsAfterReview(5)
                .codeDeletionsAfterReview(2)
                .build();

        // then
        assertAll(
                () -> assertThat(significant.hasSignificantChangesAfterReview()).isTrue(),
                () -> assertThat(insignificant.hasSignificantChangesAfterReview()).isFalse()
        );
    }
}
