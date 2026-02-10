package com.prism.statistics.domain.analysis.metadata.pullrequest.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestLabelHistoryTest {

    private static final Long GITHUB_PULL_REQUEST_ID = 100L;
    private static final Long PULL_REQUEST_ID = 1L;
    private static final String HEAD_COMMIT_SHA = "abc123";
    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void 라벨_추가_이력을_생성한다() {
        // when
        PullRequestLabelHistory history = PullRequestLabelHistory.create(
                GITHUB_PULL_REQUEST_ID, PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", PullRequestLabelAction.ADDED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(history.getLabelName()).isEqualTo("bug"),
                () -> assertThat(history.getAction()).isEqualTo(PullRequestLabelAction.ADDED),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void 라벨_삭제_이력을_생성한다() {
        // when
        PullRequestLabelHistory history = PullRequestLabelHistory.create(
                GITHUB_PULL_REQUEST_ID, PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", PullRequestLabelAction.REMOVED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID),
                () -> assertThat(history.getLabelName()).isEqualTo("bug"),
                () -> assertThat(history.getAction()).isEqualTo(PullRequestLabelAction.REMOVED),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void pullRequestId가_null이어도_생성할_수_있다() {
        // when
        PullRequestLabelHistory history = PullRequestLabelHistory.create(
                GITHUB_PULL_REQUEST_ID, null, HEAD_COMMIT_SHA, "bug", PullRequestLabelAction.ADDED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getPullRequestId()).isNull()
        );
    }

    @Test
    void GitHub_PullRequest_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(null, PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", PullRequestLabelAction.ADDED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(GITHUB_PULL_REQUEST_ID, PULL_REQUEST_ID, null, "bug", PullRequestLabelAction.ADDED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(GITHUB_PULL_REQUEST_ID, PULL_REQUEST_ID, "  ", "bug", PullRequestLabelAction.ADDED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 라벨_이름이_null이거나_빈_문자열이면_예외가_발생한다(String labelName) {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(GITHUB_PULL_REQUEST_ID, PULL_REQUEST_ID, HEAD_COMMIT_SHA, labelName, PullRequestLabelAction.ADDED, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 이름은 필수입니다.");
    }

    @Test
    void 라벨_액션이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(GITHUB_PULL_REQUEST_ID, PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", null, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 액션은 필수입니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabelHistory.create(GITHUB_PULL_REQUEST_ID, PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", PullRequestLabelAction.ADDED, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
