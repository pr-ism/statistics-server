package com.prism.statistics.domain.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestLabelTest {

    private static final Long GITHUB_PULL_REQUEST_ID = 100L;
    private static final String HEAD_COMMIT_SHA = "abc123";
    private static final LocalDateTime LABELED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void PullRequestLabel을_생성한다() {
        // when
        PullRequestLabel pullRequestLabel = PullRequestLabel.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", LABELED_AT
        );

        // then
        assertAll(
                () -> assertThat(pullRequestLabel.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(pullRequestLabel.getPullRequestId()).isNull(),
                () -> assertThat(pullRequestLabel.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(pullRequestLabel.getLabelName()).isEqualTo("bug"),
                () -> assertThat(pullRequestLabel.getLabeledAt()).isEqualTo(LABELED_AT)
        );
    }

    @Test
    void assignPullRequestId로_pullRequestId를_할당한다() {
        // given
        PullRequestLabel pullRequestLabel = PullRequestLabel.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", LABELED_AT
        );

        // when
        pullRequestLabel.assignPullRequestId(1L);

        // then
        assertThat(pullRequestLabel.getPullRequestId()).isEqualTo(1L);
    }

    @Test
    void pullRequestId가_이미_할당되어_있으면_덮어쓰지_않는다() {
        // given
        PullRequestLabel pullRequestLabel = PullRequestLabel.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", LABELED_AT
        );
        pullRequestLabel.assignPullRequestId(1L);

        // when
        pullRequestLabel.assignPullRequestId(999L);

        // then
        assertThat(pullRequestLabel.getPullRequestId()).isEqualTo(1L);
    }

    @Test
    void GitHub_PullRequest_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabel.create(null, HEAD_COMMIT_SHA, "bug", LABELED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabel.create(GITHUB_PULL_REQUEST_ID, null, "bug", LABELED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabel.create(GITHUB_PULL_REQUEST_ID, "  ", "bug", LABELED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void 라벨_이름이_null이거나_공백이면_예외가_발생한다(String labelName) {
        // when & then
        assertThatThrownBy(() -> PullRequestLabel.create(GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, labelName, LABELED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 이름은 필수입니다.");
    }

    @Test
    void 라벨_추가_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestLabel.create(GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "bug", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("라벨 추가 시각은 필수입니다.");
    }
}
