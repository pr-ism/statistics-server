package com.prism.statistics.infrastructure.collect.inbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.infrastructure.common.BoxEventTime;
import com.prism.statistics.infrastructure.common.BoxProcessingLease;
import java.time.Instant;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxTest {

    private static final CollectInboxType TYPE = CollectInboxType.PULL_REQUEST_OPENED;
    private static final Long PROJECT_ID = 1L;
    private static final long RUN_ID = 12345678L;
    private static final String PAYLOAD_JSON = "{\"pullRequest\":{\"number\":1}}";
    private static final Instant PROCESSING_STARTED_AT = Instant.parse("2026-03-16T00:00:00Z");
    private static final Instant PROCESSED_AT = Instant.parse("2026-03-16T00:01:00Z");
    private static final Instant FAILED_AT = Instant.parse("2026-03-16T00:02:00Z");

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
                () -> assertThat(inbox.getProcessingAttempt()).isZero(),
                () -> assertThat(inbox.getProcessingLease().isClaimed()).isFalse(),
                () -> assertThat(inbox.getProcessedTime().isPresent()).isFalse(),
                () -> assertThat(inbox.getFailedTime().isPresent()).isFalse(),
                () -> assertThat(inbox.getFailure().isPresent()).isFalse()
        );
    }

    @Test
    void pending_생성_시_collectType이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.pending(null, PROJECT_ID, RUN_ID, PAYLOAD_JSON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pending_생성_시_runId가_0이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.pending(TYPE, PROJECT_ID, 0L, PAYLOAD_JSON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pending_생성_시_runId가_음수이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.pending(TYPE, PROJECT_ID, -1L, PAYLOAD_JSON))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pending_생성_시_payloadJson이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rehydrate는_상태별_유효한_조합을_허용한다() {
        // when & then
        assertAll(
                () -> assertThatCode(this::createPendingInbox).doesNotThrowAnyException(),
                () -> assertThatCode(this::createProcessingInbox).doesNotThrowAnyException(),
                () -> assertThatCode(this::createProcessedInbox).doesNotThrowAnyException(),
                () -> assertThatCode(this::createRetryPendingInbox).doesNotThrowAnyException(),
                () -> assertThatCode(this::createFailedInbox).doesNotThrowAnyException()
        );
    }

    @Test
    void rehydrate_시_id가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.rehydrateBuilder()
                .id(null)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.PENDING)
                .processingAttempt(0)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.absent())
                .failure(CollectInboxFailureSnapshot.absent())
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rehydrate_시_PROCESSING_상태는_claimed_lease가_필수다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.PROCESSING)
                .processingAttempt(1)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.absent())
                .failure(CollectInboxFailureSnapshot.absent())
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rehydrate_시_PROCESSED_상태는_processedTime이_필수다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.PROCESSED)
                .processingAttempt(1)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.absent())
                .failure(CollectInboxFailureSnapshot.absent())
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rehydrate_시_RETRY_PENDING_상태는_retry_pending용_failureType이_필수다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.RETRY_PENDING)
                .processingAttempt(1)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.present(FAILED_AT))
                .failure(CollectInboxFailureSnapshot.present("실패", CollectInboxFailureType.BUSINESS_INVARIANT))
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rehydrate_시_FAILED_상태는_failed용_failureType이_필수다() {
        // when & then
        assertThatThrownBy(() -> CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.FAILED)
                .processingAttempt(1)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.present(FAILED_AT))
                .failure(CollectInboxFailureSnapshot.present("실패", CollectInboxFailureType.RETRYABLE))
                .build()
        ).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void PROCESSING_상태에서_PROCESSED로_전이한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when
        inbox.markProcessed(PROCESSED_AT);

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.PROCESSED),
                () -> assertThat(inbox.getProcessingLease().isClaimed()).isFalse(),
                () -> assertThat(inbox.getProcessedTime().isPresent()).isTrue(),
                () -> assertThat(inbox.getProcessedTime().occurredAt()).isEqualTo(PROCESSED_AT),
                () -> assertThat(inbox.getFailedTime().isPresent()).isFalse(),
                () -> assertThat(inbox.getFailure().isPresent()).isFalse()
        );
    }

    @Test
    void PENDING_상태에서_PROCESSED로_전이하면_예외가_발생한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);

        // when & then
        assertThatThrownBy(() -> inbox.markProcessed(PROCESSED_AT))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void PROCESSING_상태에서_RETRY_PENDING으로_전이한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when
        inbox.markRetryPending(FAILED_AT, "일시적 오류");

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.RETRY_PENDING),
                () -> assertThat(inbox.getProcessingLease().isClaimed()).isFalse(),
                () -> assertThat(inbox.getProcessedTime().isPresent()).isFalse(),
                () -> assertThat(inbox.getFailedTime().occurredAt()).isEqualTo(FAILED_AT),
                () -> assertThat(inbox.getFailure().reason()).isEqualTo("일시적 오류"),
                () -> assertThat(inbox.getFailure().type()).isEqualTo(CollectInboxFailureType.RETRYABLE)
        );
    }

    @Test
    void markRetryPending_시_failureReason이_blank이면_예외가_발생한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when & then
        assertThatThrownBy(() -> inbox.markRetryPending(FAILED_AT, "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markRetryPending_시_failed용_failureType이면_예외가_발생한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when & then
        assertThatThrownBy(() -> inbox.markRetryPending(
                FAILED_AT,
                "일시적 오류",
                CollectInboxFailureType.BUSINESS_INVARIANT
        )).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void PROCESSING_상태에서_BUSINESS_INVARIANT로_FAILED_전이한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when
        inbox.markFailed(FAILED_AT, "비즈니스 로직 실패", CollectInboxFailureType.BUSINESS_INVARIANT);

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.FAILED),
                () -> assertThat(inbox.getProcessingLease().isClaimed()).isFalse(),
                () -> assertThat(inbox.getProcessedTime().isPresent()).isFalse(),
                () -> assertThat(inbox.getFailedTime().occurredAt()).isEqualTo(FAILED_AT),
                () -> assertThat(inbox.getFailure().reason()).isEqualTo("비즈니스 로직 실패"),
                () -> assertThat(inbox.getFailure().type()).isEqualTo(CollectInboxFailureType.BUSINESS_INVARIANT)
        );
    }

    @Test
    void PROCESSING_상태에서_RETRY_EXHAUSTED로_FAILED_전이한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when
        inbox.markFailed(FAILED_AT, "재시도 횟수 초과", CollectInboxFailureType.RETRY_EXHAUSTED);

        // then
        assertThat(inbox.getFailure().type()).isEqualTo(CollectInboxFailureType.RETRY_EXHAUSTED);
    }

    @Test
    void PENDING_상태에서_FAILED로_전이하면_예외가_발생한다() {
        // given
        CollectInbox inbox = CollectInbox.pending(TYPE, PROJECT_ID, RUN_ID, PAYLOAD_JSON);

        // when & then
        assertThatThrownBy(() -> inbox.markFailed(
                FAILED_AT,
                "실패",
                CollectInboxFailureType.BUSINESS_INVARIANT
        )).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void markFailed_시_failureType이_null이면_예외가_발생한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when & then
        assertThatThrownBy(() -> inbox.markFailed(FAILED_AT, "실패", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void markFailed_시_retry_pending용_failureType이면_예외가_발생한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when & then
        assertThatThrownBy(() -> inbox.markFailed(FAILED_AT, "실패", CollectInboxFailureType.RETRYABLE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private CollectInbox createPendingInbox() {
        return CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.PENDING)
                .processingAttempt(0)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.absent())
                .failure(CollectInboxFailureSnapshot.absent())
                .build();
    }

    private CollectInbox createProcessingInbox() {
        return CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.PROCESSING)
                .processingAttempt(1)
                .processingLease(BoxProcessingLease.claimed(PROCESSING_STARTED_AT))
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.absent())
                .failure(CollectInboxFailureSnapshot.absent())
                .build();
    }

    private CollectInbox createProcessedInbox() {
        return CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.PROCESSED)
                .processingAttempt(1)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.present(PROCESSED_AT))
                .failedTime(BoxEventTime.absent())
                .failure(CollectInboxFailureSnapshot.absent())
                .build();
    }

    private CollectInbox createRetryPendingInbox() {
        return CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.RETRY_PENDING)
                .processingAttempt(1)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.present(FAILED_AT))
                .failure(CollectInboxFailureSnapshot.present("일시적 오류", CollectInboxFailureType.RETRYABLE))
                .build();
    }

    private CollectInbox createFailedInbox() {
        return CollectInbox.rehydrateBuilder()
                .id(1L)
                .collectType(TYPE)
                .projectId(PROJECT_ID)
                .runId(RUN_ID)
                .payloadJson(PAYLOAD_JSON)
                .status(CollectInboxStatus.FAILED)
                .processingAttempt(1)
                .processingLease(BoxProcessingLease.idle())
                .processedTime(BoxEventTime.absent())
                .failedTime(BoxEventTime.present(FAILED_AT))
                .failure(CollectInboxFailureSnapshot.present("최종 실패", CollectInboxFailureType.BUSINESS_INVARIANT))
                .build();
    }
}
