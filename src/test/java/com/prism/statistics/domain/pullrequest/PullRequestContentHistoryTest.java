package com.prism.statistics.domain.pullrequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;

import com.prism.statistics.domain.analysis.metadata.pullrequest.history.PullRequestContentHistory;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestContentHistoryTest {

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0);

    @Test
    void PullRequestContentHistory를_생성한다() {
        // given
        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(5, 100, 50);

        // when
        PullRequestContentHistory history = PullRequestContentHistory.create(1L, pullRequestChangeStats, 3, CHANGED_AT);

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getChangeStats()).isEqualTo(pullRequestChangeStats),
                () -> assertThat(history.getCommitCount()).isEqualTo(3),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // given
        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(5, 100, 50);

        // when & then
        assertThatThrownBy(() -> PullRequestContentHistory.create(null, pullRequestChangeStats, 3, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PullRequest ID는 필수입니다.");
    }

    @Test
    void 변경된_값이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestContentHistory.create(1L, null, 3, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 내역은 필수입니다.");
    }

    @Test
    void 커밋_수가_음수이면_예외가_발생한다() {
        // given
        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(5, 100, 50);

        // when & then
        assertThatThrownBy(() -> PullRequestContentHistory.create(1L, pullRequestChangeStats, -1, CHANGED_AT))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 수는 0보다 작을 수 없습니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // given
        PullRequestChangeStats pullRequestChangeStats = PullRequestChangeStats.create(5, 100, 50);

        // when & then
        assertThatThrownBy(() -> PullRequestContentHistory.create(1L, pullRequestChangeStats, 3, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
