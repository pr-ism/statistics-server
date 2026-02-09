package com.prism.statistics.domain.analysis.metadata.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime MERGED_AT = LocalDateTime.of(2024, 1, 15, 12, 30);
    private static final LocalDateTime CLOSED_AT = LocalDateTime.of(2024, 1, 15, 12, 30);

    private static final GithubUser AUTHOR = GithubUser.create("author123", 1L);
    private static final PullRequestChangeStats CHANGE_STATS = PullRequestChangeStats.create(5, 100, 50);

    @Test
    void PullRequest를_생성한다() {
        // given
        PullRequestTiming timing = PullRequestTiming.createOpen(CREATED_AT);

        // when
        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(100L)
                .projectId(1L)
                .author(AUTHOR)
                .pullRequestNumber(42)
                .headCommitSha("abc123")
                .title("feat: 새로운 기능 추가")
                .state(PullRequestState.OPEN)
                .link("https://github.com/org/repo/pull/42")
                .changeStats(CHANGE_STATS)
                .commitCount(3)
                .timing(timing)
                .build();

        // then
        assertAll(
                () -> assertThat(pullRequest.getGithubPullRequestId()).isEqualTo(100L),
                () -> assertThat(pullRequest.getProjectId()).isEqualTo(1L),
                () -> assertThat(pullRequest.getAuthor()).isEqualTo(AUTHOR),
                () -> assertThat(pullRequest.getPullRequestNumber()).isEqualTo(42),
                () -> assertThat(pullRequest.getHeadCommitSha()).isEqualTo("abc123"),
                () -> assertThat(pullRequest.getTitle()).isEqualTo("feat: 새로운 기능 추가"),
                () -> assertThat(pullRequest.getState()).isEqualTo(PullRequestState.OPEN),
                () -> assertThat(pullRequest.getLink()).isEqualTo("https://github.com/org/repo/pull/42"),
                () -> assertThat(pullRequest.getChangeStats()).isEqualTo(CHANGE_STATS),
                () -> assertThat(pullRequest.getCommitCount()).isEqualTo(3),
                () -> assertThat(pullRequest.getTiming()).isEqualTo(timing)
        );
    }

    @Test
    void GitHub_PullRequest_ID가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().githubPullRequestId(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void 프로젝트_ID가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().projectId(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트 ID는 필수입니다.");
    }

    @Test
    void 작성자_정보가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().author(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자 정보는 필수입니다.");
    }

    @Test
    void Pull_Request_번호가_0이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().pullRequestNumber(0).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 번호는 양수여야 합니다.");
    }

    @Test
    void Pull_Request_번호가_음수이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().pullRequestNumber(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 번호는 양수여야 합니다.");
    }

    @Test
    void Head_Commit_SHA가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().headCommitSha(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_빈_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().headCommitSha("  ").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void Pull_Request_제목이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().title(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 제목은 필수입니다.");
    }

    @Test
    void Pull_Request_제목이_빈_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().title("  ").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 제목은 필수입니다.");
    }

    @Test
    void Pull_Request_상태가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().state(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 상태는 필수입니다.");
    }

    @Test
    void Pull_Request_링크가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().link(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 링크는 필수입니다.");
    }

    @Test
    void Pull_Request_링크가_빈_문자열이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().link("  ").build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 링크는 필수입니다.");
    }

    @Test
    void 변경_통계가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().changeStats(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 통계는 필수입니다.");
    }

    @Test
    void 커밋_수가_음수이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().commitCount(-1).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 시간_정보가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> defaultBuilder().timing(null).build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시간 정보는 필수입니다.");
    }

    @Test
    void MERGED_상태이면_isMerged가_true를_반환한다() {
        PullRequest pullRequest = createPullRequest(PullRequestState.MERGED, PullRequestTiming.createMerged(CREATED_AT, MERGED_AT));

        assertAll(
                () -> assertThat(pullRequest.isMerged()).isTrue(),
                () -> assertThat(pullRequest.isClosed()).isFalse(),
                () -> assertThat(pullRequest.isDraft()).isFalse(),
                () -> assertThat(pullRequest.isOpen()).isFalse()
        );
    }

    @Test
    void CLOSED_상태이면_isClosed가_true를_반환한다() {
        PullRequest pullRequest = createPullRequest(PullRequestState.CLOSED, PullRequestTiming.createClosed(CREATED_AT, CLOSED_AT));

        assertAll(
                () -> assertThat(pullRequest.isClosed()).isTrue(),
                () -> assertThat(pullRequest.isMerged()).isFalse(),
                () -> assertThat(pullRequest.isDraft()).isFalse(),
                () -> assertThat(pullRequest.isOpen()).isFalse()
        );
    }

    @Test
    void DRAFT_상태이면_isDraft가_true를_반환한다() {
        PullRequest pullRequest = createPullRequest(PullRequestState.DRAFT, PullRequestTiming.createDraft(CREATED_AT));

        assertAll(
                () -> assertThat(pullRequest.isDraft()).isTrue(),
                () -> assertThat(pullRequest.isMerged()).isFalse(),
                () -> assertThat(pullRequest.isClosed()).isFalse(),
                () -> assertThat(pullRequest.isOpen()).isFalse()
        );
    }

    @Test
    void OPEN_상태이면_isOpen이_true를_반환한다() {
        PullRequest pullRequest = createPullRequest(PullRequestState.OPEN, PullRequestTiming.createOpen(CREATED_AT));

        assertAll(
                () -> assertThat(pullRequest.isOpen()).isTrue(),
                () -> assertThat(pullRequest.isMerged()).isFalse(),
                () -> assertThat(pullRequest.isClosed()).isFalse(),
                () -> assertThat(pullRequest.isDraft()).isFalse()
        );
    }

    @Test
    void MERGED_상태에서_병합_시간을_계산한다() {
        PullRequest pullRequest = createPullRequest(PullRequestState.MERGED, PullRequestTiming.createMerged(CREATED_AT, MERGED_AT));

        long mergeTimeMinutes = pullRequest.calculateMergeTimeMinutes();

        assertThat(mergeTimeMinutes).isEqualTo(150);
    }

    @Test
    void MERGED_상태가_아니면_병합_시간_계산시_예외가_발생한다() {
        PullRequest pullRequest = createPullRequest(PullRequestState.OPEN, PullRequestTiming.createOpen(CREATED_AT));

        assertThatThrownBy(() -> pullRequest.calculateMergeTimeMinutes())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("병합되지 않은 PullRequest 입니다.");
    }

    private PullRequest.PullRequestBuilder defaultBuilder() {
        return PullRequest.builder()
                .githubPullRequestId(100L)
                .projectId(1L)
                .author(AUTHOR)
                .pullRequestNumber(42)
                .headCommitSha("abc123")
                .title("title")
                .state(PullRequestState.OPEN)
                .link("link")
                .changeStats(CHANGE_STATS)
                .commitCount(3)
                .timing(PullRequestTiming.createOpen(CREATED_AT));
    }

    private PullRequest createPullRequest(PullRequestState state, PullRequestTiming timing) {
        return defaultBuilder()
                .state(state)
                .timing(timing)
                .build();
    }
}
