package com.prism.statistics.domain.analysis.insight.bottleneck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.domain.analysis.insight.bottleneck.PullRequestBottleneck.BottleneckType;
import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestBottleneckTest {

    @Test
    void PR_정체_구간을_생성한다() {
        // given
        Long pullRequestId = 1L;
        DurationMinutes reviewWait = DurationMinutes.of(60L);
        DurationMinutes reviewProgress = DurationMinutes.of(120L);
        DurationMinutes mergeWait = DurationMinutes.of(30L);
        LocalDateTime firstReviewAt = LocalDateTime.of(2024, 1, 1, 11, 0);
        LocalDateTime lastReviewAt = LocalDateTime.of(2024, 1, 1, 13, 0);
        LocalDateTime lastApproveAt = LocalDateTime.of(2024, 1, 1, 13, 0);

        // when
        PullRequestBottleneck bottleneck = PullRequestBottleneck.builder()
                .pullRequestId(pullRequestId)
                .reviewWait(reviewWait)
                .reviewProgress(reviewProgress)
                .mergeWait(mergeWait)
                .firstReviewAt(firstReviewAt)
                .lastReviewAt(lastReviewAt)
                .lastApproveAt(lastApproveAt)
                .build();

        // then
        assertAll(
                () -> assertThat(bottleneck.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(bottleneck.getReviewWait()).isEqualTo(reviewWait),
                () -> assertThat(bottleneck.getReviewProgress()).isEqualTo(reviewProgress),
                () -> assertThat(bottleneck.getMergeWait()).isEqualTo(mergeWait),
                () -> assertThat(bottleneck.getFirstReviewAt()).isEqualTo(firstReviewAt),
                () -> assertThat(bottleneck.getLastReviewAt()).isEqualTo(lastReviewAt),
                () -> assertThat(bottleneck.getLastApproveAt()).isEqualTo(lastApproveAt)
        );
    }

    @Test
    void 리뷰가_없는_PR_정체_구간을_생성한다() {
        // when
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createWithoutReview(1L);

        // then
        assertAll(
                () -> assertThat(bottleneck.getReviewWait()).isNull(),
                () -> assertThat(bottleneck.getReviewProgress()).isEqualTo(DurationMinutes.zero()),
                () -> assertThat(bottleneck.getMergeWait()).isNull(),
                () -> assertThat(bottleneck.hasReview()).isFalse()
        );
    }

    @Test
    void 첫_리뷰_등록_시_정체_구간을_생성한다() {
        // given
        Long pullRequestId = 1L;
        LocalDateTime reviewReadyAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime firstReviewAt = LocalDateTime.of(2024, 1, 1, 11, 0);

        // when
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                pullRequestId, reviewReadyAt, firstReviewAt
        );

        // then
        assertAll(
                () -> assertThat(bottleneck.getReviewWait().getMinutes()).isEqualTo(60L),
                () -> assertThat(bottleneck.getReviewProgress()).isEqualTo(DurationMinutes.zero()),
                () -> assertThat(bottleneck.getFirstReviewAt()).isEqualTo(firstReviewAt),
                () -> assertThat(bottleneck.getLastReviewAt()).isEqualTo(firstReviewAt),
                () -> assertThat(bottleneck.hasReview()).isTrue()
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> PullRequestBottleneck.createWithoutReview(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pull Request ID는 필수입니다.");
    }

    @Test
    void 새로운_리뷰_등록_시_업데이트한다() {
        // given
        LocalDateTime reviewReadyAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime firstReviewAt = LocalDateTime.of(2024, 1, 1, 11, 0);
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                1L, reviewReadyAt, firstReviewAt
        );
        LocalDateTime secondReviewAt = LocalDateTime.of(2024, 1, 1, 13, 0);

        // when
        bottleneck.updateOnNewReview(secondReviewAt, false);

        // then
        assertAll(
                () -> assertThat(bottleneck.getLastReviewAt()).isEqualTo(secondReviewAt),
                () -> assertThat(bottleneck.getReviewProgress().getMinutes()).isEqualTo(120L),
                () -> assertThat(bottleneck.getLastApproveAt()).isNull()
        );
    }

    @Test
    void Approve_리뷰_등록_시_업데이트한다() {
        // given
        LocalDateTime reviewReadyAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime firstReviewAt = LocalDateTime.of(2024, 1, 1, 11, 0);
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                1L, reviewReadyAt, firstReviewAt
        );
        LocalDateTime approveAt = LocalDateTime.of(2024, 1, 1, 14, 0);

        // when
        bottleneck.updateOnNewReview(approveAt, true);

        // then
        assertAll(
                () -> assertThat(bottleneck.getLastReviewAt()).isEqualTo(approveAt),
                () -> assertThat(bottleneck.getLastApproveAt()).isEqualTo(approveAt),
                () -> assertThat(bottleneck.hasApproval()).isTrue()
        );
    }

    @Test
    void PR_병합_시_업데이트한다() {
        // given
        LocalDateTime reviewReadyAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime firstReviewAt = LocalDateTime.of(2024, 1, 1, 11, 0);
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                1L, reviewReadyAt, firstReviewAt
        );
        LocalDateTime approveAt = LocalDateTime.of(2024, 1, 1, 14, 0);
        bottleneck.updateOnNewReview(approveAt, true);
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 1, 14, 30);

        // when
        bottleneck.updateOnMerge(mergedAt);

        // then
        assertAll(
                () -> assertThat(bottleneck.getMergeWait().getMinutes()).isEqualTo(30L),
                () -> assertThat(bottleneck.isMerged()).isTrue()
        );
    }

    @Test
    void 총_정체_시간을_계산한다() {
        // given
        PullRequestBottleneck bottleneck = PullRequestBottleneck.builder()
                .pullRequestId(1L)
                .reviewWait(DurationMinutes.of(60L))
                .reviewProgress(DurationMinutes.of(120L))
                .mergeWait(DurationMinutes.of(30L))
                .firstReviewAt(LocalDateTime.now())
                .lastReviewAt(LocalDateTime.now())
                .lastApproveAt(LocalDateTime.now())
                .build();

        // when
        DurationMinutes total = bottleneck.getTotalBottleneckTime();

        // then
        assertThat(total.getMinutes()).isEqualTo(210L);
    }

    @Test
    void 가장_긴_정체_구간을_식별한다() {
        // given
        PullRequestBottleneck reviewWaitLongest = PullRequestBottleneck.builder()
                .pullRequestId(1L)
                .reviewWait(DurationMinutes.of(120L))
                .reviewProgress(DurationMinutes.of(60L))
                .mergeWait(DurationMinutes.of(30L))
                .firstReviewAt(LocalDateTime.now())
                .lastReviewAt(LocalDateTime.now())
                .lastApproveAt(LocalDateTime.now())
                .build();

        PullRequestBottleneck reviewProgressLongest = PullRequestBottleneck.builder()
                .pullRequestId(2L)
                .reviewWait(DurationMinutes.of(30L))
                .reviewProgress(DurationMinutes.of(180L))
                .mergeWait(DurationMinutes.of(60L))
                .firstReviewAt(LocalDateTime.now())
                .lastReviewAt(LocalDateTime.now())
                .lastApproveAt(LocalDateTime.now())
                .build();

        PullRequestBottleneck mergeWaitLongest = PullRequestBottleneck.builder()
                .pullRequestId(3L)
                .reviewWait(DurationMinutes.of(30L))
                .reviewProgress(DurationMinutes.of(60L))
                .mergeWait(DurationMinutes.of(200L))
                .firstReviewAt(LocalDateTime.now())
                .lastReviewAt(LocalDateTime.now())
                .lastApproveAt(LocalDateTime.now())
                .build();

        // then
        assertAll(
                () -> assertThat(reviewWaitLongest.getLongestBottleneck()).isEqualTo(BottleneckType.REVIEW_WAIT),
                () -> assertThat(reviewProgressLongest.getLongestBottleneck()).isEqualTo(BottleneckType.REVIEW_PROGRESS),
                () -> assertThat(mergeWaitLongest.getLongestBottleneck()).isEqualTo(BottleneckType.MERGE_WAIT)
        );
    }

    @Test
    void 정체_구간이_없으면_NONE을_반환한다() {
        // when
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createWithoutReview(1L);

        // then
        assertThat(bottleneck.getLongestBottleneck()).isEqualTo(BottleneckType.NONE);
    }

    @Test
    void 첫_리뷰가_없는_상태에서_리뷰_등록_시_첫_리뷰로_설정된다() {
        // given
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createWithoutReview(1L);
        LocalDateTime reviewAt = LocalDateTime.of(2024, 1, 1, 11, 0);

        // when
        bottleneck.updateOnNewReview(reviewAt, false);

        // then
        assertAll(
                () -> assertThat(bottleneck.getFirstReviewAt()).isEqualTo(reviewAt),
                () -> assertThat(bottleneck.getLastReviewAt()).isEqualTo(reviewAt),
                () -> assertThat(bottleneck.hasReview()).isTrue()
        );
    }

    @Test
    void Approve_없이_병합_시_mergeWait이_설정되지_않는다() {
        // given
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                1L,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 1, 11, 0)
        );
        LocalDateTime mergedAt = LocalDateTime.of(2024, 1, 1, 14, 0);

        // when
        bottleneck.updateOnMerge(mergedAt);

        // then
        assertAll(
                () -> assertThat(bottleneck.getMergeWait()).isNull(),
                () -> assertThat(bottleneck.isMerged()).isFalse()
        );
    }

    @Test
    void 리뷰가_없는_PR의_승인_및_병합_상태를_확인한다() {
        // given
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createWithoutReview(1L);

        // then
        assertAll(
                () -> assertThat(bottleneck.hasApproval()).isFalse(),
                () -> assertThat(bottleneck.isMerged()).isFalse()
        );
    }

    @Test
    void 일부_정체_구간만_존재할_때_총_정체_시간을_계산한다() {
        // given
        PullRequestBottleneck bottleneck = PullRequestBottleneck.createOnFirstReview(
                1L,
                LocalDateTime.of(2024, 1, 1, 10, 0),
                LocalDateTime.of(2024, 1, 1, 11, 0)
        );

        // when
        DurationMinutes total = bottleneck.getTotalBottleneckTime();

        // then
        assertThat(total.getMinutes()).isEqualTo(60L);
    }
}
