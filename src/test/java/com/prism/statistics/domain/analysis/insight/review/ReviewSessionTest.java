package com.prism.statistics.domain.analysis.insight.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewSessionTest {

    @Test
    void 리뷰_세션을_생성한다() {
        // given
        Long pullRequestId = 1L;
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime firstActivityAt = LocalDateTime.of(2024, 1, 1, 10, 0);

        // when
        ReviewSession session = ReviewSession.create(pullRequestId, reviewer, firstActivityAt);

        // then
        assertAll(
                () -> assertThat(session.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(session.getReviewer()).isEqualTo(reviewer),
                () -> assertThat(session.getFirstActivityAt()).isEqualTo(firstActivityAt),
                () -> assertThat(session.getLastActivityAt()).isEqualTo(firstActivityAt),
                () -> assertThat(session.getSessionDuration()).isEqualTo(DurationMinutes.zero()),
                () -> assertThat(session.getReviewCount()).isEqualTo(1),
                () -> assertThat(session.getCommentCount()).isZero()
        );
    }

    @Test
    void 코멘트와_함께_리뷰_세션을_생성한다() {
        // given
        Long pullRequestId = 1L;
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime firstActivityAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        int initialCommentCount = 3;

        // when
        ReviewSession session = ReviewSession.createWithComment(
                pullRequestId, reviewer, firstActivityAt, initialCommentCount
        );

        // then
        assertAll(
                () -> assertThat(session.getReviewCount()).isZero(),
                () -> assertThat(session.getCommentCount()).isEqualTo(initialCommentCount)
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // given
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime activityAt = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> ReviewSession.create(null, reviewer, activityAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pull Request ID는 필수입니다.");
    }

    @Test
    void 리뷰어가_null이면_예외가_발생한다() {
        // given
        LocalDateTime activityAt = LocalDateTime.now();

        // when & then
        assertThatThrownBy(() -> ReviewSession.create(1L, null, activityAt))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("리뷰어 정보는 필수입니다.");
    }

    @Test
    void 활동_시각이_null이면_예외가_발생한다() {
        // given
        GithubUser reviewer = GithubUser.create("reviewer", 123L);

        // when & then
        assertThatThrownBy(() -> ReviewSession.create(1L, reviewer, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("활동 시각은 필수입니다.");
    }

    @Test
    void 새로운_리뷰_등록_시_업데이트한다() {
        // given
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime firstActivityAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewSession session = ReviewSession.create(1L, reviewer, firstActivityAt);
        LocalDateTime secondReviewAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        int newCommentCount = 5;

        // when
        session.updateOnReview(secondReviewAt, newCommentCount);

        // then
        assertAll(
                () -> assertThat(session.getLastActivityAt()).isEqualTo(secondReviewAt),
                () -> assertThat(session.getSessionDuration().getMinutes()).isEqualTo(120L),
                () -> assertThat(session.getReviewCount()).isEqualTo(2),
                () -> assertThat(session.getCommentCount()).isEqualTo(newCommentCount)
        );
    }

    @Test
    void 새로운_코멘트_등록_시_업데이트한다() {
        // given
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime firstActivityAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewSession session = ReviewSession.create(1L, reviewer, firstActivityAt);
        LocalDateTime commentAt = LocalDateTime.of(2024, 1, 1, 11, 30);

        // when
        session.updateOnComment(commentAt);

        // then
        assertAll(
                () -> assertThat(session.getLastActivityAt()).isEqualTo(commentAt),
                () -> assertThat(session.getSessionDuration().getMinutes()).isEqualTo(90L),
                () -> assertThat(session.getCommentCount()).isEqualTo(1)
        );
    }

    @Test
    void 이전_시각_활동은_마지막_활동_시각을_업데이트하지_않는다() {
        // given
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime firstActivityAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewSession session = ReviewSession.create(1L, reviewer, firstActivityAt);
        LocalDateTime laterActivityAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        session.updateOnComment(laterActivityAt);
        LocalDateTime earlierActivityAt = LocalDateTime.of(2024, 1, 1, 11, 0);

        // when
        session.updateOnComment(earlierActivityAt);

        // then
        assertAll(
                () -> assertThat(session.getLastActivityAt()).isEqualTo(laterActivityAt),
                () -> assertThat(session.getSessionDuration().getMinutes()).isEqualTo(120L)
        );
    }

    @Test
    void 단일_활동_여부를_확인한다() {
        // given
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime activityAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewSession singleActivity = ReviewSession.create(1L, reviewer, activityAt);

        ReviewSession multipleActivities = ReviewSession.create(1L, reviewer, activityAt);
        multipleActivities.updateOnComment(LocalDateTime.of(2024, 1, 1, 11, 0));

        // then
        assertAll(
                () -> assertThat(singleActivity.isSingleActivity()).isTrue(),
                () -> assertThat(multipleActivities.isSingleActivity()).isFalse()
        );
    }

    @Test
    void 총_활동_수를_계산한다() {
        // given
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime activityAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewSession session = ReviewSession.create(1L, reviewer, activityAt);
        session.updateOnReview(LocalDateTime.of(2024, 1, 1, 11, 0), 3);
        session.updateOnComment(LocalDateTime.of(2024, 1, 1, 12, 0));

        // when
        int totalActivities = session.getTotalActivities();

        // then
        assertThat(totalActivities).isEqualTo(6);
    }

    @Test
    void 활성_리뷰어_여부를_확인한다() {
        // given
        GithubUser reviewer = GithubUser.create("reviewer", 123L);
        LocalDateTime activityAt = LocalDateTime.of(2024, 1, 1, 10, 0);

        ReviewSession withReview = ReviewSession.create(1L, reviewer, activityAt);
        ReviewSession withOnlyComment = ReviewSession.createWithComment(1L, reviewer, activityAt, 5);

        // then
        assertAll(
                () -> assertThat(withReview.isActiveReviewer()).isTrue(),
                () -> assertThat(withOnlyComment.isActiveReviewer()).isFalse()
        );
    }
}
