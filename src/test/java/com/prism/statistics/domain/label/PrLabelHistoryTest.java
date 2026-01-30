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

import com.prism.statistics.domain.label.enums.LabelAction;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PrLabelHistoryTest {

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void 라벨_추가_이력을_생성한다() {
        // when
        PrLabelHistory prLabelHistory = PrLabelHistory.create(1L, "bug", LabelAction.ADDED, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(prLabelHistory.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(prLabelHistory.getLabelName()).isEqualTo("bug"),
                () -> assertThat(prLabelHistory.getAction()).isEqualTo(LabelAction.ADDED),
                () -> assertThat(prLabelHistory.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void 라벨_삭제_이력을_생성한다() {
        // when
        PrLabelHistory prLabelHistory = PrLabelHistory.create(1L, "bug", LabelAction.REMOVED, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(prLabelHistory.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(prLabelHistory.getLabelName()).isEqualTo("bug"),
                () -> assertThat(prLabelHistory.getAction()).isEqualTo(LabelAction.REMOVED),
                () -> assertThat(prLabelHistory.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrLabelHistory.create(null, "bug", LabelAction.ADDED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 라벨_이름이_null이거나_빈_문자열이면_예외가_발생한다(String labelName) {
        // when & then
        assertThatThrownBy(() -> PrLabelHistory.create(1L, labelName, LabelAction.ADDED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 이름은 필수입니다.");
    }

    @Test
    void 라벨_액션이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrLabelHistory.create(1L, "bug", null, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 액션은 필수입니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PrLabelHistory.create(1L, "bug", LabelAction.ADDED, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
