package com.prism.statistics.domain.analysis.metadata.review.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewerAction;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RequestedReviewerHistoryTest {

    private static final Long GITHUB_PULL_REQUEST_ID = 1L;
    private static final String HEAD_COMMIT_SHA = "abc123def456";
    private static final GithubUser REVIEWER = GithubUser.create("reviewer1", 12345L);
    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void 리뷰어_요청_이력을_생성한다() {
        // when
        RequestedReviewerHistory history = RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, ReviewerAction.REQUESTED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(history.getReviewer()).isEqualTo(REVIEWER),
                () -> assertThat(history.getAction()).isEqualTo(ReviewerAction.REQUESTED),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void 리뷰어_제거_이력을_생성한다() {
        // when
        RequestedReviewerHistory history = RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, ReviewerAction.REMOVED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(history.getReviewer()).isEqualTo(REVIEWER),
                () -> assertThat(history.getAction()).isEqualTo(ReviewerAction.REMOVED),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void Github_Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                null, HEAD_COMMIT_SHA, REVIEWER, ReviewerAction.REQUESTED, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, null, REVIEWER, ReviewerAction.REQUESTED, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, "  ", REVIEWER, ReviewerAction.REQUESTED, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void 리뷰어가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, null, ReviewerAction.REQUESTED, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어는 필수입니다.");
    }

    @Test
    void 리뷰어_액션이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, null, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어 액션은 필수입니다.");
    }

    @Test
    void assignPullRequestId로_pullRequestId를_할당한다() {
        // given
        RequestedReviewerHistory history = RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, ReviewerAction.REQUESTED, CHANGED_AT
        );

        // when
        history.assignPullRequestId(1L);

        // then
        assertThat(history.getPullRequestId()).isEqualTo(1L);
    }

    @Test
    void pullRequestId가_이미_할당되어_있으면_덮어쓰지_않는다() {
        // given
        RequestedReviewerHistory history = RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, ReviewerAction.REQUESTED, CHANGED_AT
        );
        history.assignPullRequestId(1L);

        // when
        history.assignPullRequestId(999L);

        // then
        assertThat(history.getPullRequestId()).isEqualTo(1L);
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, ReviewerAction.REQUESTED, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
