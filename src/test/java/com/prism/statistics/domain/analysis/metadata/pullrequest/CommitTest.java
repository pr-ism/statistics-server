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

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CommitTest {

    private static final Long PULL_REQUEST_ID = 1L;
    private static final Long GITHUB_PULL_REQUEST_ID = 100L;
    private static final String COMMIT_SHA = "abc123def456";
    private static final LocalDateTime COMMITTED_AT = LocalDateTime.of(2024, 1, 15, 10, 30);

    @Test
    void Commit을_생성한다() {
        // when
        Commit commit = Commit.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, COMMIT_SHA, COMMITTED_AT);

        // then
        assertAll(
                () -> assertThat(commit.getPullRequestId()).isEqualTo(PULL_REQUEST_ID),
                () -> assertThat(commit.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(commit.getCommitSha()).isEqualTo(COMMIT_SHA),
                () -> assertThat(commit.getCommittedAt()).isEqualTo(COMMITTED_AT)
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Commit.create(null, GITHUB_PULL_REQUEST_ID, COMMIT_SHA, COMMITTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @Test
    void GitHub_Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Commit.create(PULL_REQUEST_ID, null, COMMIT_SHA, COMMITTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 커밋_SHA가_null이거나_빈_문자열이면_예외가_발생한다(String commitSha) {
        // when & then
        assertThatThrownBy(() -> Commit.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, commitSha, COMMITTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 SHA는 필수입니다.");
    }

    @Test
    void 커밋_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Commit.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, COMMIT_SHA, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 시각은 필수입니다.");
    }

    @Test
    void pullRequestId_없이_Commit을_생성한다() {
        // when
        Commit commit = Commit.createEarly(GITHUB_PULL_REQUEST_ID, COMMIT_SHA, COMMITTED_AT);

        // then
        assertAll(
                () -> assertThat(commit.getPullRequestId()).isNull(),
                () -> assertThat(commit.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(commit.getCommitSha()).isEqualTo(COMMIT_SHA),
                () -> assertThat(commit.getCommittedAt()).isEqualTo(COMMITTED_AT)
        );
    }

    @Test
    void createEarly에서_GitHub_Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Commit.createEarly(null, COMMIT_SHA, COMMITTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createEarly에서_커밋_SHA가_null이거나_빈_문자열이면_예외가_발생한다(String commitSha) {
        // when & then
        assertThatThrownBy(() -> Commit.createEarly(GITHUB_PULL_REQUEST_ID, commitSha, COMMITTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 SHA는 필수입니다.");
    }

    @Test
    void createEarly에서_커밋_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Commit.createEarly(GITHUB_PULL_REQUEST_ID, COMMIT_SHA, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 시각은 필수입니다.");
    }

    @Test
    void pullRequestId가_null일_때_할당한다() {
        // given
        Commit commit = Commit.createEarly(GITHUB_PULL_REQUEST_ID, COMMIT_SHA, COMMITTED_AT);

        // when
        commit.assignPullRequestId(PULL_REQUEST_ID);

        // then
        assertThat(commit.getPullRequestId()).isEqualTo(PULL_REQUEST_ID);
    }

    @Test
    void pullRequestId가_이미_있으면_변경하지_않는다() {
        // given
        Commit commit = Commit.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, COMMIT_SHA, COMMITTED_AT);

        // when
        commit.assignPullRequestId(999L);

        // then
        assertThat(commit.getPullRequestId()).isEqualTo(PULL_REQUEST_ID);
    }

    @Test
    void pullRequestId가_있으면_hasAssignedPullRequestId가_true를_반환한다() {
        // when
        Commit commit = Commit.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, COMMIT_SHA, COMMITTED_AT);

        // then
        assertThat(commit.hasAssignedPullRequestId()).isTrue();
    }

    @Test
    void pullRequestId가_없으면_hasAssignedPullRequestId가_false를_반환한다() {
        // when
        Commit commit = Commit.createEarly(GITHUB_PULL_REQUEST_ID, COMMIT_SHA, COMMITTED_AT);

        // then
        assertThat(commit.hasAssignedPullRequestId()).isFalse();
    }
}
