package com.prism.statistics.infrastructure.collect.inbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Instant;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxTest {

    private static final CollectInboxType TYPE = CollectInboxType.PULL_REQUEST_OPENED;
    private static final Long PROJECT_ID = 1L;
    private static final Long RUN_ID = 12345678L;
    private static final String PAYLOAD_JSON = "{\"pullRequest\":{\"number\":1}}";

    @Test
    void PENDING_상태의_inbox를_생성한다() {
        // when
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);

        // then
        assertAll(
                () -> assertThat(inbox.getCollectType()).isEqualTo(TYPE),
                () -> assertThat(inbox.getProjectId()).isEqualTo(PROJECT_ID),
                () -> assertThat(inbox.getRunId()).isEqualTo(RUN_ID),
                () -> assertThat(inbox.getPayloadJson()).isEqualTo(PAYLOAD_JSON),
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.PENDING),
                () -> assertThat(inbox.getProcessingAttempt()).isZero()
        );
    }

    @Test
    void pending_생성_시_collectType이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> CollectInbox.pending(null, PROJECT_ID, RUN_ID, PAYLOAD_JSON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pending_생성_시_runId가_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> CollectInbox.pending(TYPE, PROJECT_ID, null, PAYLOAD_JSON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pending_생성_시_runId가_0이면_정상_생성된다() {
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, 0L, PAYLOAD_JSON);

        assertThat(inbox.getRunId()).isZero();
    }

    @Test
    void pending_생성_시_runId가_음수이면_예외가_발생한다() {
        assertThatThrownBy(() -> CollectInbox.pending(TYPE, PROJECT_ID, -1L, PAYLOAD_JSON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pending_생성_시_payloadJson이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void PENDING_상태에서_PROCESSING으로_전이한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();

        // when
        inbox.markProcessing(now);

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.PROCESSING),
                () -> assertThat(inbox.getProcessingAttempt()).isEqualTo(1),
                () -> assertThat(inbox.getProcessingStartedAt()).isEqualTo(now),
                () -> assertThat(inbox.getFailureReason()).isNull(),
                () -> assertThat(inbox.getFailureType()).isNull(),
                () -> assertThat(inbox.getFailedAt()).isNull()
        );
    }

    @Test
    void RETRY_PENDING_상태에서_PROCESSING으로_전이한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();
        inbox.markProcessing(now);
        inbox.markRetryPending(now, "일시적 오류");

        // when
        inbox.markProcessing(now);

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.PROCESSING),
                () -> assertThat(inbox.getProcessingAttempt()).isEqualTo(2)
        );
    }

    @Test
    void PROCESSED_상태에서_PROCESSING으로_전이하면_예외가_발생한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();
        inbox.markProcessing(now);
        inbox.markProcessed(now);

        // when & then
        assertThatThrownBy(() -> inbox.markProcessing(now))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markProcessing_시_processingStartedAt이_null이면_예외가_발생한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);

        // when & then
        assertThatThrownBy(() -> inbox.markProcessing(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void PROCESSING_상태에서_PROCESSED로_전이한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();
        inbox.markProcessing(now);

        // when
        inbox.markProcessed(now);

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.PROCESSED),
                () -> assertThat(inbox.getProcessingStartedAt()).isNull(),
                () -> assertThat(inbox.getProcessedAt()).isEqualTo(now),
                () -> assertThat(inbox.getFailureReason()).isNull(),
                () -> assertThat(inbox.getFailureType()).isNull()
        );
    }

    @Test
    void PENDING_상태에서_PROCESSED로_전이하면_예외가_발생한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);

        // when & then
        assertThatThrownBy(() -> inbox.markProcessed(Instant.now()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void PROCESSING_상태에서_RETRY_PENDING으로_전이한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();
        inbox.markProcessing(now);

        // when
        inbox.markRetryPending(now, "일시적 오류");

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.RETRY_PENDING),
                () -> assertThat(inbox.getProcessingStartedAt()).isNull(),
                () -> assertThat(inbox.getFailedAt()).isEqualTo(now),
                () -> assertThat(inbox.getFailureReason()).isEqualTo("일시적 오류"),
                () -> assertThat(inbox.getFailureType()).isNull()
        );
    }

    @Test
    void markRetryPending_시_failureReason이_blank이면_예외가_발생한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();
        inbox.markProcessing(now);

        // when & then
        assertThatThrownBy(() -> inbox.markRetryPending(now, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void PROCESSING_상태에서_BUSINESS_INVARIANT로_FAILED_전이한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();
        inbox.markProcessing(now);

        // when
        inbox.markFailed(now, "비즈니스 로직 실패", CollectInboxFailureType.BUSINESS_INVARIANT);

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.FAILED),
                () -> assertThat(inbox.getProcessingStartedAt()).isNull(),
                () -> assertThat(inbox.getFailedAt()).isEqualTo(now),
                () -> assertThat(inbox.getFailureReason()).isEqualTo("비즈니스 로직 실패"),
                () -> assertThat(inbox.getFailureType()).isEqualTo(CollectInboxFailureType.BUSINESS_INVARIANT)
        );
    }

    @Test
    void PROCESSING_상태에서_RETRY_EXHAUSTED로_FAILED_전이한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();
        inbox.markProcessing(now);

        // when
        inbox.markFailed(now, "재시도 횟수 초과", CollectInboxFailureType.RETRY_EXHAUSTED);

        // then
        assertThat(inbox.getFailureType()).isEqualTo(CollectInboxFailureType.RETRY_EXHAUSTED);
    }

    @Test
    void PENDING_상태에서_FAILED로_전이하면_예외가_발생한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);

        // when & then
        assertThatThrownBy(() -> inbox.markFailed(
                Instant.now(), "실패", CollectInboxFailureType.BUSINESS_INVARIANT
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markFailed_시_failureType이_null이면_예외가_발생한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);
        Instant now = Instant.now();
        inbox.markProcessing(now);

        // when & then
        assertThatThrownBy(() -> inbox.markFailed(now, "실패", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
