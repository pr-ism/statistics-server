package com.prism.statistics.domain.pullrequest.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PreviousFileNameTest {

    @Test
    void 이전_파일명을_생성한다() {
        // when
        PreviousFileName previousFileName = PreviousFileName.of("OldName.java");

        // then
        assertAll(
                () -> assertThat(previousFileName.getValue()).isEqualTo("OldName.java"),
                () -> assertThat(previousFileName.isPresent()).isTrue(),
                () -> assertThat(previousFileName.isEmpty()).isFalse()
        );
    }

    @Test
    void 빈_이전_파일명을_생성한다() {
        // when
        PreviousFileName previousFileName = PreviousFileName.empty();

        // then
        assertAll(
                () -> assertThat(previousFileName.getValue()).isNull(),
                () -> assertThat(previousFileName.isPresent()).isFalse(),
                () -> assertThat(previousFileName.isEmpty()).isTrue()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 이전_파일명이_null이거나_빈_문자열이면_예외가_발생한다(String value) {
        // when & then
        assertThatThrownBy(() -> PreviousFileName.of(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이전 파일명은 필수입니다.");
    }

    @Test
    void 공백만_있는_이전_파일명은_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PreviousFileName.of("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이전 파일명은 필수입니다.");
    }

    @Test
    void 동등성을_비교한다() {
        // given
        PreviousFileName previousFileName1 = PreviousFileName.of("OldName.java");
        PreviousFileName previousFileName2 = PreviousFileName.of("OldName.java");

        // then
        assertThat(previousFileName1).isEqualTo(previousFileName2);
    }

    @Test
    void 값이_다르면_동등하지_않다() {
        // given
        PreviousFileName previousFileName1 = PreviousFileName.of("OldName1.java");
        PreviousFileName previousFileName2 = PreviousFileName.of("OldName2.java");

        // then
        assertThat(previousFileName1).isNotEqualTo(previousFileName2);
    }

    @Test
    void 빈_객체끼리는_동등하다() {
        // given
        PreviousFileName empty1 = PreviousFileName.empty();
        PreviousFileName empty2 = PreviousFileName.empty();

        // then
        assertThat(empty1).isEqualTo(empty2);
    }
}
