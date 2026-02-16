package com.prism.statistics.domain.analysis.insight.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.domain.analysis.insight.vo.DurationMinutes;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewResponseTimeTest {

    @Test
    void 리뷰_반응_시간을_생성한다() {
        // given
        Long pullRequestId = 1L;
        DurationMinutes responseAfterReview = DurationMinutes.of(30L);
        DurationMinutes changesResolution = DurationMinutes.of(120L);
        LocalDateTime lastChangesRequestedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime firstCommitAfterChangesAt = LocalDateTime.of(2024, 1, 1, 10, 30);
        LocalDateTime firstApproveAfterChangesAt = LocalDateTime.of(2024, 1, 1, 12, 0);
        int changesRequestedCount = 2;

        // when
        ReviewResponseTime responseTime = ReviewResponseTime.builder()
                .pullRequestId(pullRequestId)
                .responseAfterReview(responseAfterReview)
                .changesResolution(changesResolution)
                .lastChangesRequestedAt(lastChangesRequestedAt)
                .firstCommitAfterChangesAt(firstCommitAfterChangesAt)
                .firstApproveAfterChangesAt(firstApproveAfterChangesAt)
                .changesRequestedCount(changesRequestedCount)
                .build();

        // then
        assertAll(
                () -> assertThat(responseTime.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(responseTime.getResponseAfterReview()).isEqualTo(responseAfterReview),
                () -> assertThat(responseTime.getChangesResolution()).isEqualTo(changesResolution),
                () -> assertThat(responseTime.getChangesRequestedCount()).isEqualTo(changesRequestedCount),
                () -> assertThat(responseTime.hasChangesRequested()).isTrue(),
                () -> assertThat(responseTime.hasResponded()).isTrue(),
                () -> assertThat(responseTime.isResolved()).isTrue()
        );
    }

    @Test
    void Request_Changes가_없는_PR을_생성한다() {
        // when
        ReviewResponseTime responseTime = ReviewResponseTime.createWithoutChangesRequested(1L);

        // then
        assertAll(
                () -> assertThat(responseTime.getResponseAfterReview()).isNull(),
                () -> assertThat(responseTime.getChangesResolution()).isNull(),
                () -> assertThat(responseTime.getChangesRequestedCount()).isZero(),
                () -> assertThat(responseTime.hasChangesRequested()).isFalse()
        );
    }

    @Test
    void 첫_Request_Changes_발생_시_생성한다() {
        // given
        LocalDateTime changesRequestedAt = LocalDateTime.of(2024, 1, 1, 10, 0);

        // when
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(1L, changesRequestedAt);

        // then
        assertAll(
                () -> assertThat(responseTime.getChangesRequestedCount()).isEqualTo(1),
                () -> assertThat(responseTime.getLastChangesRequestedAt()).isEqualTo(changesRequestedAt),
                () -> assertThat(responseTime.hasChangesRequested()).isTrue(),
                () -> assertThat(responseTime.hasResponded()).isFalse()
        );
    }

    @Test
    void PR_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewResponseTime.createWithoutChangesRequested(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pull Request ID는 필수입니다.");
    }

    @Test
    void Request_Changes_횟수가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ReviewResponseTime.builder()
                .pullRequestId(1L)
                .changesRequestedCount(-1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Request Changes 횟수는 0보다 작을 수 없습니다.");
    }

    @Test
    void Request_Changes_발생_시_업데이트한다() {
        // given
        LocalDateTime firstChangesAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(1L, firstChangesAt);
        LocalDateTime secondChangesAt = LocalDateTime.of(2024, 1, 1, 14, 0);

        // when
        responseTime.updateOnChangesRequested(secondChangesAt);

        // then
        assertAll(
                () -> assertThat(responseTime.getChangesRequestedCount()).isEqualTo(2),
                () -> assertThat(responseTime.getLastChangesRequestedAt()).isEqualTo(secondChangesAt),
                () -> assertThat(responseTime.getResponseAfterReview()).isNull(),
                () -> assertThat(responseTime.getChangesResolution()).isNull()
        );
    }

    @Test
    void RC_이후_커밋_발생_시_업데이트한다() {
        // given
        LocalDateTime changesRequestedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(1L, changesRequestedAt);
        LocalDateTime committedAt = LocalDateTime.of(2024, 1, 1, 10, 45);

        // when
        responseTime.updateOnCommitAfterChanges(committedAt);

        // then
        assertAll(
                () -> assertThat(responseTime.getFirstCommitAfterChangesAt()).isEqualTo(committedAt),
                () -> assertThat(responseTime.getResponseAfterReview().getMinutes()).isEqualTo(45L),
                () -> assertThat(responseTime.hasResponded()).isTrue()
        );
    }

    @Test
    void RC_이전_커밋은_무시한다() {
        // given
        LocalDateTime changesRequestedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(1L, changesRequestedAt);
        LocalDateTime earlierCommit = LocalDateTime.of(2024, 1, 1, 9, 0);

        // when
        responseTime.updateOnCommitAfterChanges(earlierCommit);

        // then
        assertThat(responseTime.getFirstCommitAfterChangesAt()).isNull();
    }

    @Test
    void RC_이후_Approve_발생_시_업데이트한다() {
        // given
        LocalDateTime changesRequestedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(1L, changesRequestedAt);
        LocalDateTime approvedAt = LocalDateTime.of(2024, 1, 1, 12, 0);

        // when
        responseTime.updateOnApproveAfterChanges(approvedAt);

        // then
        assertAll(
                () -> assertThat(responseTime.getFirstApproveAfterChangesAt()).isEqualTo(approvedAt),
                () -> assertThat(responseTime.getChangesResolution().getMinutes()).isEqualTo(120L),
                () -> assertThat(responseTime.isResolved()).isTrue()
        );
    }

    @Test
    void RC_업데이트_시_null이면_예외가_발생한다() {
        // given
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(
                1L, LocalDateTime.of(2024, 1, 1, 10, 0)
        );

        // when & then
        assertThatThrownBy(() -> responseTime.updateOnChangesRequested(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경 요청 시각은(는) 필수입니다.");
    }

    @Test
    void 커밋_시각이_null이면_예외가_발생한다() {
        // given
        ReviewResponseTime responseTime = ReviewResponseTime.createOnChangesRequested(
                1L, LocalDateTime.of(2024, 1, 1, 10, 0)
        );

        // when & then
        assertThatThrownBy(() -> responseTime.updateOnCommitAfterChanges(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("커밋 시각은(는) 필수입니다.");
    }

    @Test
    void RC가_없으면_커밋_업데이트가_무시된다() {
        // given
        ReviewResponseTime responseTime = ReviewResponseTime.createWithoutChangesRequested(1L);
        LocalDateTime committedAt = LocalDateTime.now();

        // when
        responseTime.updateOnCommitAfterChanges(committedAt);

        // then
        assertThat(responseTime.getFirstCommitAfterChangesAt()).isNull();
    }
}
