package com.prism.statistics.domain.label;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrLabelTest {

    private static final LocalDateTime LABELED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void PrLabel을_생성한다() {
        // when
        PrLabel prLabel = PrLabel.create(1L, "bug", LABELED_AT);

        // then
        assertAll(
                () -> assertThat(prLabel.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(prLabel.getLabelName()).isEqualTo("bug"),
                () -> assertThat(prLabel.getLabeledAt()).isEqualTo(LABELED_AT)
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrLabel.create(null, "bug", LABELED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 라벨_이름이_null이거나_빈_문자열이면_예외가_발생한다(String labelName) {
        // when & then
        assertThatThrownBy(() -> PrLabel.create(1L, labelName, LABELED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 이름은 필수입니다.");
    }

    @Test
    void 라벨_추가_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrLabel.create(1L, "bug", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 추가 시각은 필수입니다.");
    }
}
