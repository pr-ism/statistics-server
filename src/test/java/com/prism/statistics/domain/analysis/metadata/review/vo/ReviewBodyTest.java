package com.prism.statistics.domain.analysis.metadata.review.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prism.statistics.domain.analysis.metadata.review.vo.ReviewBody;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewBodyTest {

    @Test
    void create로_리뷰_본문을_생성한다() {
        // given
        String value = "LGTM";

        // when
        ReviewBody reviewBody = ReviewBody.create(value);

        // then
        assertThat(reviewBody.getValue()).isEqualTo("LGTM");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void create에서_null이거나_빈_문자열이면_빈_본문을_반환한다(String value) {
        // when
        ReviewBody reviewBody = ReviewBody.create(value);

        // then
        assertThat(reviewBody.getValue()).isEmpty();
    }

    @Test
    void createRequired로_리뷰_본문을_생성한다() {
        // given
        String value = "코드 리뷰 내용입니다.";

        // when
        ReviewBody reviewBody = ReviewBody.createRequired(value);

        // then
        assertThat(reviewBody.getValue()).isEqualTo("코드 리뷰 내용입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void createRequired에서_null이거나_빈_문자열이면_예외가_발생한다(String value) {
        // when & then
        assertThatThrownBy(() -> ReviewBody.createRequired(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰 본문은 필수입니다.");
    }

    @Test
    void isEmpty는_빈_본문이면_true를_반환한다() {
        // given
        ReviewBody reviewBody = ReviewBody.create(null);

        // when & then
        assertThat(reviewBody.isEmpty()).isTrue();
    }

    @Test
    void isEmpty는_본문이_있으면_false를_반환한다() {
        // given
        ReviewBody reviewBody = ReviewBody.create("LGTM");

        // when & then
        assertThat(reviewBody.isEmpty()).isFalse();
    }

    @Test
    void 동등성을_비교한다() {
        // given
        ReviewBody reviewBody1 = ReviewBody.create("LGTM");
        ReviewBody reviewBody2 = ReviewBody.create("LGTM");

        // then
        assertThat(reviewBody1).isEqualTo(reviewBody2);
    }
}
