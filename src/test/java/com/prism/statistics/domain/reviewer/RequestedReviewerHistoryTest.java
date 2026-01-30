package com.prism.statistics.domain.reviewer;

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

import com.prism.statistics.domain.reviewer.enums.ReviewerAction;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RequestedReviewerHistoryTest {

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void 리뷰어_요청_이력을_생성한다() {
        // when
        RequestedReviewerHistory history = RequestedReviewerHistory.create(
                1L, "reviewer1", 12345L, ReviewerAction.REQUESTED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getGithubMention()).isEqualTo("reviewer1"),
                () -> assertThat(history.getGithubUid()).isEqualTo(12345L),
                () -> assertThat(history.getAction()).isEqualTo(ReviewerAction.REQUESTED),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void 리뷰어_제거_이력을_생성한다() {
        // when
        RequestedReviewerHistory history = RequestedReviewerHistory.create(
                1L, "reviewer1", 12345L, ReviewerAction.REMOVED, CHANGED_AT
        );

        // then
        assertAll(
                () -> assertThat(history.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(history.getGithubMention()).isEqualTo("reviewer1"),
                () -> assertThat(history.getGithubUid()).isEqualTo(12345L),
                () -> assertThat(history.getAction()).isEqualTo(ReviewerAction.REMOVED),
                () -> assertThat(history.getChangedAt()).isEqualTo(CHANGED_AT)
        );
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                null, "reviewer1", 12345L, ReviewerAction.REQUESTED, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void GitHub_멘션이_null이거나_빈_문자열이면_예외가_발생한다(String githubMention) {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                1L, githubMention, 12345L, ReviewerAction.REQUESTED, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub 멘션은 필수입니다.");
    }

    @Test
    void GitHub_UID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                1L, "reviewer1", null, ReviewerAction.REQUESTED, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("GitHub UID는 필수입니다.");
    }

    @Test
    void 리뷰어_액션이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                1L, "reviewer1", 12345L, null, CHANGED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어 액션은 필수입니다.");
    }

    @Test
    void 변경_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewerHistory.create(
                1L, "reviewer1", 12345L, ReviewerAction.REQUESTED, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 시각은 필수입니다.");
    }
}
