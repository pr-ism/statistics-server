package com.prism.statistics.domain.analysis.metadata.pullrequest.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.FileChanges;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestFileHistoryTest {

    private static final Long PULL_REQUEST_ID = 1L;
    private static final Long GITHUB_PULL_REQUEST_ID = 100L;
    private static final String HEAD_COMMIT_SHA = "abc123";
    private static final FileChanges FILE_CHANGES = FileChanges.create(100, 50);
    private static final LocalDateTime GITHUB_CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void PullRequestFileHistory를_생성한다() {
        // when
        PullRequestFileHistory history = PullRequestFileHistory.create(
                PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA,
                "src/main/java/com/example/Service.java", FileChangeType.MODIFIED,
                FILE_CHANGES, GITHUB_CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID),
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(history.getFileName()).isEqualTo("src/main/java/com/example/Service.java"),
                () -> assertThat(history.getPreviousFileName().isEmpty()).isTrue(),
                () -> assertThat(history.getChangeType()).isEqualTo(FileChangeType.MODIFIED),
                () -> assertThat(history.getFileChanges()).isEqualTo(FILE_CHANGES),
                () -> assertThat(history.getGithubChangedAt()).isEqualTo(GITHUB_CHANGED_AT),
                () -> assertThat(history.isRenamed()).isFalse()
        );
    }

    @Test
    void RENAMED_파일_이력을_생성한다() {
        // when
        PullRequestFileHistory history = PullRequestFileHistory.createRenamed(
                PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA,
                "NewName.java", "OldName.java",
                FILE_CHANGES, GITHUB_CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID),
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(history.getFileName()).isEqualTo("NewName.java"),
                () -> assertThat(history.getPreviousFileName().getValue()).isEqualTo("OldName.java"),
                () -> assertThat(history.getChangeType()).isEqualTo(FileChangeType.RENAMED),
                () -> assertThat(history.getFileChanges()).isEqualTo(FILE_CHANGES),
                () -> assertThat(history.getGithubChangedAt()).isEqualTo(GITHUB_CHANGED_AT),
                () -> assertThat(history.isRenamed()).isTrue()
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.create(null, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", FileChangeType.ADDED, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @Test
    void GitHub_Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.create(PULL_REQUEST_ID, null, HEAD_COMMIT_SHA, "file.java", FileChangeType.ADDED, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, null, "file.java", FileChangeType.ADDED, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @Test
    void Head_Commit_SHA가_빈_문자열이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, "  ", "file.java", FileChangeType.ADDED, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 파일명이_null이거나_빈_문자열이면_예외가_발생한다(String fileName) {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, fileName, FileChangeType.ADDED, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일명은 필수입니다.");
    }

    @Test
    void 변경_타입이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", null, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 타입은 필수입니다.");
    }

    @Test
    void create에서_RENAMED_타입도_생성할_수_있다() {
        // when
        PullRequestFileHistory history = PullRequestFileHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", FileChangeType.RENAMED, FILE_CHANGES, GITHUB_CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(history.getChangeType()).isEqualTo(FileChangeType.RENAMED),
                () -> assertThat(history.getPreviousFileName().isEmpty()).isTrue(),
                () -> assertThat(history.isRenamed()).isFalse()
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createRenamed에서_이전_파일명이_null이거나_빈_문자열이면_예외가_발생한다(String previousFileName) {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.createRenamed(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "NewName.java", previousFileName, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이전 파일명은 필수입니다.");
    }

    @Test
    void 파일_변경_정보가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", FileChangeType.ADDED, null, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 변경 정보는 필수입니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", FileChangeType.ADDED, FILE_CHANGES, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }

    @Test
    void 총_변경_라인_수를_계산한다() {
        // when
        PullRequestFileHistory history = PullRequestFileHistory.create(PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", FileChangeType.MODIFIED, FILE_CHANGES, GITHUB_CHANGED_AT);

        // then
        assertThat(history.getTotalChanges()).isEqualTo(150);
    }

    @Test
    void pullRequestId_없이_생성한다() {
        // when
        PullRequestFileHistory history = PullRequestFileHistory.createEarly(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA,
                "file.java", FileChangeType.MODIFIED,
                FILE_CHANGES, GITHUB_CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isNull(),
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getHeadCommitSha()).isEqualTo(HEAD_COMMIT_SHA),
                () -> assertThat(history.getFileName()).isEqualTo("file.java"),
                () -> assertThat(history.isRenamed()).isFalse()
        );
    }

    @Test
    void pullRequestId_없이_RENAMED_파일_이력을_생성한다() {
        // when
        PullRequestFileHistory history = PullRequestFileHistory.createEarlyRenamed(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA,
                "NewName.java", "OldName.java",
                FILE_CHANGES, GITHUB_CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isNull(),
                () -> assertThat(history.getGithubPullRequestId()).isEqualTo(GITHUB_PULL_REQUEST_ID),
                () -> assertThat(history.getFileName()).isEqualTo("NewName.java"),
                () -> assertThat(history.getPreviousFileName().getValue()).isEqualTo("OldName.java"),
                () -> assertThat(history.getChangeType()).isEqualTo(FileChangeType.RENAMED),
                () -> assertThat(history.isRenamed()).isTrue()
        );
    }

    @Test
    void createEarly에서_GitHub_Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.createEarly(null, HEAD_COMMIT_SHA, "file.java", FileChangeType.ADDED, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub PullRequest ID는 필수입니다.");
    }

    @Test
    void createEarly에서_Head_Commit_SHA가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.createEarly(GITHUB_PULL_REQUEST_ID, null, "file.java", FileChangeType.ADDED, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Head Commit SHA는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createEarly에서_파일명이_null이거나_빈_문자열이면_예외가_발생한다(String fileName) {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.createEarly(GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, fileName, FileChangeType.ADDED, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일명은 필수입니다.");
    }

    @Test
    void createEarly에서_변경_타입이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.createEarly(GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", null, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 타입은 필수입니다.");
    }

    @Test
    void createEarly에서_파일_변경_정보가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.createEarly(GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", FileChangeType.ADDED, null, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("파일 변경 정보는 필수입니다.");
    }

    @Test
    void createEarly에서_변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.createEarly(GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "file.java", FileChangeType.ADDED, FILE_CHANGES, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createEarlyRenamed에서_이전_파일명이_null이거나_빈_문자열이면_예외가_발생한다(String previousFileName) {
        // when & then
        assertThatThrownBy(() -> PullRequestFileHistory.createEarlyRenamed(GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA, "NewName.java", previousFileName, FILE_CHANGES, GITHUB_CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이전 파일명은 필수입니다.");
    }

    @Test
    void pullRequestId가_null일_때_할당한다() {
        // given
        PullRequestFileHistory history = PullRequestFileHistory.createEarly(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA,
                "file.java", FileChangeType.MODIFIED,
                FILE_CHANGES, GITHUB_CHANGED_AT
        );

        // when
        history.assignPullRequestId(PULL_REQUEST_ID);

        // then
        assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID);
    }

    @Test
    void pullRequestId가_이미_있으면_변경하지_않는다() {
        // given
        PullRequestFileHistory history = PullRequestFileHistory.create(
                PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA,
                "file.java", FileChangeType.MODIFIED,
                FILE_CHANGES, GITHUB_CHANGED_AT
        );

        // when
        history.assignPullRequestId(999L);

        // then
        assertThat(history.getPullRequestId()).isEqualTo(PULL_REQUEST_ID);
    }

    @Test
    void pullRequestId가_있으면_hasAssignedPullRequestId가_true를_반환한다() {
        // when
        PullRequestFileHistory history = PullRequestFileHistory.create(
                PULL_REQUEST_ID, GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA,
                "file.java", FileChangeType.MODIFIED,
                FILE_CHANGES, GITHUB_CHANGED_AT
        );

        // then
        assertThat(history.hasAssignedPullRequestId()).isTrue();
    }

    @Test
    void pullRequestId가_없으면_hasAssignedPullRequestId가_false를_반환한다() {
        // when
        PullRequestFileHistory history = PullRequestFileHistory.createEarly(
                GITHUB_PULL_REQUEST_ID, HEAD_COMMIT_SHA,
                "file.java", FileChangeType.MODIFIED,
                FILE_CHANGES, GITHUB_CHANGED_AT
        );

        // then
        assertThat(history.hasAssignedPullRequestId()).isFalse();
    }
}
