package com.prism.statistics.domain.label;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestLabelTest {

    private static final LocalDateTime LABELED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void PullRequestLabel을_생성한다() {
        // when
        PullRequestLabel pullRequestLabel = PullRequestLabel.create(1L, "bug", LABELED_AT);

        // then
        assertAll(
                () -> assertThat(pullRequestLabel.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(pullRequestLabel.getLabelName()).isEqualTo("bug"),
                () -> assertThat(pullRequestLabel.getLabeledAt()).isEqualTo(LABELED_AT)
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabel.create(null, "bug", LABELED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void 라벨_이름이_null이거나_공백이면_예외가_발생한다(String labelName) {
        // when & then
        assertThatThrownBy(() -> PullRequestLabel.create(1L, labelName, LABELED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 이름은 필수입니다.");
    }

    @Test
    void 라벨_추가_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabel.create(1L, "bug", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 추가 시각은 필수입니다.");
    }
}
