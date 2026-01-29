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

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class RequestedReviewerTest {

    private static final LocalDateTime REQUESTED_AT = LocalDateTime.of(2024, 1, 15, 10, 0, 0);

    @Test
    void 리뷰어_할당을_생성한다() {
        // when
        RequestedReviewer reviewer = RequestedReviewer.create(
                1L, "reviewer1", 12345L, REQUESTED_AT
        );

        // then
        assertAll(
                () -> assertThat(reviewer.getPullRequestId()).isEqualTo(1L),
                () -> assertThat(reviewer.getReviewerUsername()).isEqualTo("reviewer1"),
                () -> assertThat(reviewer.getReviewerGithubId()).isEqualTo(12345L),
                () -> assertThat(reviewer.getRequestedAt()).isEqualTo(REQUESTED_AT)
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                null, "reviewer1", 12345L, REQUESTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("PR ID는 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 리뷰어_사용자명이_null이거나_빈_문자열이면_예외가_발생한다(String reviewerUsername) {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                1L, reviewerUsername, 12345L, REQUESTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어 사용자명은 필수입니다.");
    }

    @Test
    void 리뷰어_GitHub_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                1L, "reviewer1", null, REQUESTED_AT
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어 GitHub ID는 필수입니다.");
    }

    @Test
    void 리뷰어_요청_시각이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> RequestedReviewer.create(
                1L, "reviewer1", 12345L, null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어 요청 시각은 필수입니다.");
    }
}
