package com.prism.statistics.domain.analysis.metadata.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RequestedReviewerTest {

    private static final Long GITHUB_PULL_REQUEST_ID = 100L;
    private static final String HEAD_COMMIT_SHA = "abc123def456";
    private static final LocalDateTime GITHUB_REQUESTED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);
    private static final GithubUser REVIEWER = GithubUser.create("reviewer1", 12345L);

    @Test
    void 리뷰어_할당을_생성한다() {
        // when
        RequestedReviewer reviewer = RequestedReviewer.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, GITHUB_REQUESTED_AT
        );

        // then
        assertAll(
                () -> assertThat(reviewer.getPullRequestId()).isNull(),
                () -> assertThat(reviewer.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(reviewer.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(reviewer.getReviewer()).isEqualTo(REVIEWER),
                () -> assertThat(reviewer.getGithubRequestedAt()).isEqualTo(GITHUB_REQUESTED_AT)
        );
    }

    @Test
    void Github_Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                null, HEAD_COMMIT_SHA, REVIEWER, GITHUB_REQUESTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                GITHUB_PULL_REQUEST_ID, null, REVIEWER, GITHUB_REQUESTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                GITHUB_PULL_REQUEST_ID, "  ", REVIEWER, GITHUB_REQUESTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void 리뷰어가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, null, GITHUB_REQUESTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어는 필수입니다.");
    }

    @Test
    void assignPullRequestId로_pullRequestId를_할당한다() {
        // given
        RequestedReviewer reviewer = RequestedReviewer.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, GITHUB_REQUESTED_AT
        );

        // when
        reviewer.assignPullRequestId(1L);

        // then
        assertThat(reviewer.getPullRequestId()).isEqualTo(1L);
    }

    @Test
    void pullRequestId가_이미_할당되어_있으면_덮어쓰지_않는다() {
        // given
        RequestedReviewer reviewer = RequestedReviewer.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, GITHUB_REQUESTED_AT
        );
        reviewer.assignPullRequestId(1L);

        // when
        reviewer.assignPullRequestId(999L);

        // then
        assertThat(reviewer.getPullRequestId()).isEqualTo(1L);
    }

    @Test
    void 리뷰어_요청_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, REVIEWER, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어 요청 시각은 필수입니다.");
    }
}
