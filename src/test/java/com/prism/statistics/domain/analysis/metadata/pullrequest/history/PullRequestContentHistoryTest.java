package com.prism.statistics.domain.analysis.metadata.pullrequest.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestContentHistoryTest {

    private static final Long PULL_REQUEST_ID = 1L;
    private static final Long GITHUB_PULL_REQUEST_ID = 100L;
    private static final String HEAD_COMMIT_SHA = "abc123";
    private static final PullRequestChangeStats CHANGE_STATS = PullRequestChangeStats.create(5, 100, 50);
    private static final int COMMIT_COUNT = 3;
    private static final LocalDateTime GITHUB_CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void PullRequestContentHistory를_생성한다() {
        PullRequestContentHistory history = PullRequestContentHistory.create(
                PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT
        );

        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID),
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(history.getChangeStats()).isEqualTo(CHANGE_STATS),
                () -> assertThat(history.getCommitCount()).isEqualTo(COMMIT_COUNT),
                () -> assertThat(history.getGithubChangedAt()).isEqualTo(GITHUB_CHANGED_AT)
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestContentHistory.create(null, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @Test
    void GitHub_Pull_Request_ID가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestContentHistory.create(PULL_REQUEST_ID, null, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestContentHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, null, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_빈_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestContentHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, "  ", CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void 변경된_값이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestContentHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, null, COMMIT_COUNT, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 내역은 필수입니다.");
    }

    @Test
    void 커밋_수가_음수이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestContentHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, -1, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestContentHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }

    @Test
    void pullRequestId_없이_생성한다() {
        PullRequestContentHistory history = PullRequestContentHistory.createEarly(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT
        );

        assertAll(
                () -> assertThat(history.getPullRequestId()).isNull(),
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(history.getChangeStats()).isEqualTo(CHANGE_STATS),
                () -> assertThat(history.getCommitCount()).isEqualTo(COMMIT_COUNT),
                () -> assertThat(history.getGithubChangedAt()).isEqualTo(GITHUB_CHANGED_AT)
        );
    }

    @Test
    void createEarly에서_GitHub_Pull_Request_ID가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> PullRequestContentHistory.createEarly(null, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void pullRequestId가_null일_때_할당한다() {
        PullRequestContentHistory history = PullRequestContentHistory.createEarly(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT
        );

        history.assignPullRequestId(PULL_REQUEST_ID);

        assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID);
    }

    @Test
    void pullRequestId가_이미_있으면_변경하지_않는다() {
        PullRequestContentHistory history = PullRequestContentHistory.create(
                PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT
        );

        history.assignPullRequestId(999L);

        assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID);
    }

    @Test
    void pullRequestId가_있으면_hasAssignedPullRequestId가_true를_반환한다() {
        PullRequestContentHistory history = PullRequestContentHistory.create(
                PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT
        );

        assertThat(history.hasAssignedPullRequestId()).isTrue();
    }

    @Test
    void pullRequestId가_없으면_hasAssignedPullRequestId가_false를_반환한다() {
        PullRequestContentHistory history = PullRequestContentHistory.createEarly(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, CHANGE_STATS, COMMIT_COUNT, GITHUB_CHANGED_AT
        );

        assertThat(history.hasAssignedPullRequestId()).isFalse();
    }
}
