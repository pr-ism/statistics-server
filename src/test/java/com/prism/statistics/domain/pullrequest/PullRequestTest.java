package com.prism.statistics.domain.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.pullrequest.vo.PullRequestTiming;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);
    private static final LocalDateTime MERGED_AT = LocalDateTime.of(2024, 1, 15, 12, 30);
    private static final LocalDateTime CLOSED_AT = LocalDateTime.of(2024, 1, 15, 12, 30);

    @Test
    void PullRequest를_생성한다() {
        // given
        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(5, 100, 50);
        PullRequestTiming pullRequestTiming = PullRequestTiming.createOpen(CREATED_AT);

        // when
        PullRequest pullRequest = PullRequest.create(
                1L, "author123", 42, "feat: 새로운 기능 추가",
                PullRequestState.OPEN, "https://github.com/org/repo/pull/42",
                pullRequestChangeStats, 3, pullRequestTiming
        );

        // then
        assertAll(
                () -> assertThat(pullRequest.getProjectId()).isEqualTo(1L),
                () -> assertThat(pullRequest.getAuthorGithubId()).isEqualTo("author123"),
                () -> assertThat(pullRequest.getPullRequestNumber()).isEqualTo(42),
                () -> assertThat(pullRequest.getTitle()).isEqualTo("feat: 새로운 기능 추가"),
                () -> assertThat(pullRequest.getState()).isEqualTo(PullRequestState.OPEN),
                () -> assertThat(pullRequest.getLink()).isEqualTo("https://github.com/org/repo/pull/42"),
                () -> assertThat(pullRequest.getChangeStats()).isEqualTo(pullRequestChangeStats),
                () -> assertThat(pullRequest.getCommitCount()).isEqualTo(3),
                () -> assertThat(pullRequest.getTiming()).isEqualTo(pullRequestTiming)
        );
    }

    @Test
    void opened로_생성하면_OPEN_상태가_된다() {
        // given
        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(5, 100, 50);
        PullRequestTiming pullRequestTiming = PullRequestTiming.createOpen(CREATED_AT);

        // when
        PullRequest pullRequest = PullRequest.opened(
                1L, "author123", 42, "feat: 새로운 기능 추가",
                "https://github.com/org/repo/pull/42",
                pullRequestChangeStats, 3, pullRequestTiming
        );

        // then
        assertThat(pullRequest.getState()).isEqualTo(PullRequestState.OPEN);
    }

    @Test
    void 프로젝트_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(null, "author123", 42, "title", PullRequestState.OPEN, "link", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트 ID는 필수입니다.");
    }

    @Test
    void 작성자_GitHub_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, null, 42, "title", PullRequestState.OPEN, "link", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자 GitHub ID는 필수입니다.");
    }

    @Test
    void 작성자_GitHub_ID가_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "  ", 42, "title", PullRequestState.OPEN, "link", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("작성자 GitHub ID는 필수입니다.");
    }

    @Test
    void Pull_Request_번호가_0이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "author123", 0, "title", PullRequestState.OPEN, "link", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 번호는 양수여야 합니다.");
    }

    @Test
    void Pull_Request_번호가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "author123", -1, "title", PullRequestState.OPEN, "link", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 번호는 양수여야 합니다.");
    }

    @Test
    void Pull_Request_제목이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "author123", 42, null, PullRequestState.OPEN, "link", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 제목은 필수입니다.");
    }

    @Test
    void Pull_Request_제목이_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "author123", 42, "  ", PullRequestState.OPEN, "link", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 제목은 필수입니다.");
    }

    @Test
    void Pull_Request_상태가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "author123", 42, "title", null, "link", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 상태는 필수입니다.");
    }

    @Test
    void Pull_Request_링크가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "author123", 42, "title", PullRequestState.OPEN, null, 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 링크는 필수입니다.");
    }

    @Test
    void Pull_Request_링크가_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "author123", 42, "title", PullRequestState.OPEN, "  ", 3))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest 링크는 필수입니다.");
    }

    @Test
    void 커밋_수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> createPullRequest(1L, "author123", 42, "title", PullRequestState.OPEN, "link", -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 변경_통계가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequest.create(
                1L, "author123", 42, "title", PullRequestState.OPEN, "link",
                null, 3, PullRequestTiming.createOpen(CREATED_AT)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 통계는 필수입니다.");
    }

    @Test
    void 시간_정보가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequest.create(
                1L, "author123", 42, "title", PullRequestState.OPEN, "link",
                PullRequestChangeStats.create(5, 100, 50), 3, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("시간 정보는 필수입니다.");
    }

    @Test
    void MERGED_상태이면_isMerged가_true를_반환한다() {
        // given
        PullRequest pullRequest = createMergedPullRequest();

        // when & then
        assertAll(
                () -> assertThat(pullRequest.isMerged()).isTrue(),
                () -> assertThat(pullRequest.isClosed()).isFalse(),
                () -> assertThat(pullRequest.isDraft()).isFalse(),
                () -> assertThat(pullRequest.isOpen()).isFalse()
        );
    }

    @Test
    void CLOSED_상태이면_isClosed가_true를_반환한다() {
        // given
        PullRequest pullRequest = createClosedPullRequest();

        // when & then
        assertAll(
                () -> assertThat(pullRequest.isClosed()).isTrue(),
                () -> assertThat(pullRequest.isMerged()).isFalse(),
                () -> assertThat(pullRequest.isDraft()).isFalse(),
                () -> assertThat(pullRequest.isOpen()).isFalse()
        );
    }

    @Test
    void DRAFT_상태이면_isDraft가_true를_반환한다() {
        // given
        PullRequest pullRequest = createDraftPullRequest();

        // when & then
        assertAll(
                () -> assertThat(pullRequest.isDraft()).isTrue(),
                () -> assertThat(pullRequest.isMerged()).isFalse(),
                () -> assertThat(pullRequest.isClosed()).isFalse(),
                () -> assertThat(pullRequest.isOpen()).isFalse()
        );
    }

    @Test
    void OPEN_상태이면_isOpen이_true를_반환한다() {
        // given
        PullRequest pullRequest = createOpenPullRequest();

        // when & then
        assertAll(
                () -> assertThat(pullRequest.isOpen()).isTrue(),
                () -> assertThat(pullRequest.isMerged()).isFalse(),
                () -> assertThat(pullRequest.isClosed()).isFalse(),
                () -> assertThat(pullRequest.isDraft()).isFalse()
        );
    }

    @Test
    void MERGED_상태에서_병합_시간을_계산한다() {
        // given
        PullRequest pullRequest = createMergedPullRequest();

        // when
        long mergeTimeMinutes = pullRequest.calculateMergeTimeMinutes();

        // then
        assertThat(mergeTimeMinutes).isEqualTo(150);
    }

    @Test
    void MERGED_상태가_아니면_병합_시간_계산시_예외가_발생한다() {
        // given
        PullRequest pullRequest = createOpenPullRequest();

        // when & then
        assertThatThrownBy(() -> pullRequest.calculateMergeTimeMinutes())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("병합되지 않은 PullRequest입니다.");
    }

    private PullRequest createPullRequest(
            Long projectId, String authorGithubId, int pullRequestNumber,
            String title, PullRequestState state, String link, int commitCount
    ) {
        return PullRequest.create(
                projectId, authorGithubId, pullRequestNumber, title, state, link,
                PullRequestChangeStats.create(5, 100, 50), commitCount, PullRequestTiming.createOpen(CREATED_AT)
        );
    }

    private PullRequest createOpenPullRequest() {
        return PullRequest.create(
                1L, "author123", 42, "title", PullRequestState.OPEN, "link",
                PullRequestChangeStats.create(5, 100, 50), 3, PullRequestTiming.createOpen(CREATED_AT)
        );
    }

    private PullRequest createDraftPullRequest() {
        return PullRequest.create(
                1L, "author123", 42, "title", PullRequestState.DRAFT, "link",
                PullRequestChangeStats.create(5, 100, 50), 3, PullRequestTiming.createDraft(CREATED_AT)
        );
    }

    private PullRequest createClosedPullRequest() {
        return PullRequest.create(
                1L, "author123", 42, "title", PullRequestState.CLOSED, "link",
                PullRequestChangeStats.create(5, 100, 50), 3, PullRequestTiming.createClosed(CREATED_AT, CLOSED_AT)
        );
    }

    private PullRequest createMergedPullRequest() {
        return PullRequest.create(
                1L, "author123", 42, "title", PullRequestState.MERGED, "link",
                PullRequestChangeStats.create(5, 100, 50), 3, PullRequestTiming.createMerged(CREATED_AT, MERGED_AT, CLOSED_AT)
        );
    }
}
