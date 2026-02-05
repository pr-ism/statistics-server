package com.prism.statistics.domain.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.pullrequest.Commit;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CommitTest {

    private static final LocalDateTime COMMITTED_AT = LocalDateTime.of(2024, 1, 15, 10, 30);

    @Test
    void Commit을_생성한다() {
        // when
        Commit commit = Commit.create(1L, "abc123def456", COMMITTED_AT);

        // then
        assertAll(
                () -> assertThat(commit.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(commit.getCommitSha()).isEqualTo("abc123def456"),
                () -> assertThat(commit.getCommittedAt()).isEqualTo(COMMITTED_AT)
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Commit.create(null, "abc123", COMMITTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 커밋_SHA가_null이거나_빈_문자열이면_예외가_발생한다(String commitSha) {
        // when & then
        assertThatThrownBy(() -> Commit.create(1L, commitSha, COMMITTED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 SHA는 필수입니다.");
    }

    @Test
    void 커밋_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Commit.create(1L, "abc123", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 시각은 필수입니다.");
    }
}
