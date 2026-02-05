package com.prism.statistics.domain.label;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestLabelHistory;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestLabelHistoryTest {

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void 라벨_추가_이력을_생성한다() {
        // when
        PullRequestLabelHistory pullRequestLabelHistory = PullRequestLabelHistory.create(1L, "bug", PullRequestLabelAction.ADDED, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(pullRequestLabelHistory.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(pullRequestLabelHistory.getLabelName()).isEqualTo("bug"),
                () -> assertThat(pullRequestLabelHistory.getAction()).isEqualTo(PullRequestLabelAction.ADDED),
                () -> assertThat(pullRequestLabelHistory.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void 라벨_삭제_이력을_생성한다() {
        // when
        PullRequestLabelHistory pullRequestLabelHistory = PullRequestLabelHistory.create(1L, "bug", PullRequestLabelAction.REMOVED, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(pullRequestLabelHistory.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(pullRequestLabelHistory.getLabelName()).isEqualTo("bug"),
                () -> assertThat(pullRequestLabelHistory.getAction()).isEqualTo(PullRequestLabelAction.REMOVED),
                () -> assertThat(pullRequestLabelHistory.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(null, "bug", PullRequestLabelAction.ADDED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 라벨_이름이_null이거나_빈_문자열이면_예외가_발생한다(String labelName) {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(1L, labelName, PullRequestLabelAction.ADDED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 이름은 필수입니다.");
    }

    @Test
    void 라벨_액션이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(1L, "bug", null, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 액션은 필수입니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(1L, "bug", PullRequestLabelAction.ADDED, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
