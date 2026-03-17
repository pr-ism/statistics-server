package com.prism.statistics.application.collect.inbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.prism.statistics.global.config.properties.CollectRetryProperties;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxFailureType;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxStatus;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.infrastructure.collect.inbox.repository.CollectInboxRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxEntryProcessorTest {

    @Mock
    CollectInboxRepository collectInboxRepository;

    @Mock
    CollectInboxClaimedExecutor collectInboxClaimedExecutor;

    CollectInboxEntryProcessor collectInboxEntryProcessor;

    @BeforeEach
    void setUp() {
        CollectRetryProperties retryProperties = new CollectRetryProperties(3);

        collectInboxEntryProcessor = new CollectInboxEntryProcessor(
                Clock.fixed(Instant.parse("2026-03-16T00:00:00Z"), ZoneId.of("Asia/Seoul")),
                retryProperties,
                collectInboxRepository,
                collectInboxClaimedExecutor,
                new CollectInboxFailureReasonTruncator(),
                new CollectRetryExceptionClassifier()
        );
    }

    @Test
    void markProcessing_선점에_실패하면_처리를_건너뛴다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);
        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(false);

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        verify(collectInboxRepository, never()).findById(anyLong());
        verify(collectInboxClaimedExecutor, never()).execute(any());
    }

    @Test
    void markProcessing_성공후_inbox_재조회에_실패하면_추가_처리없이_종료한다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);
        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.empty());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        verify(collectInboxClaimedExecutor, never()).execute(any());
    }

    @Test
    void 정상처리시_executor가_호출된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(11L);

        CollectInbox actual = createProcessingInbox(1L, 1);

        given(collectInboxRepository.markProcessingIfClaimable(eq(11L), any())).willReturn(true);
        given(collectInboxRepository.findById(11L)).willReturn(Optional.of(actual));

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        verify(collectInboxClaimedExecutor).execute(actual);
    }

    @Test
    void 재시도_가능한_예외_발생시_첫_시도면_RETRY_PENDING으로_마킹된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = createProcessingInbox(2L, 1);

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new QueryTimeoutException("DB 타임아웃"))
                .given(collectInboxClaimedExecutor)
                .execute(any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertAll(
                () -> assertThat(actual.getStatus()).isEqualTo(CollectInboxStatus.RETRY_PENDING),
                () -> assertThat(actual.getFailureType()).isNull()
        );
        verify(collectInboxRepository).save(actual);
    }

    @Test
    void 재시도_불가능한_예외_발생시_즉시_FAILED와_BUSINESS_INVARIANT로_마킹된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = createProcessingInbox(20L, 1);

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new IllegalArgumentException("비즈니스 로직 위반"))
                .given(collectInboxClaimedExecutor)
                .execute(any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertAll(
                () -> assertThat(actual.getStatus()).isEqualTo(CollectInboxStatus.FAILED),
                () -> assertThat(actual.getFailureType()).isEqualTo(CollectInboxFailureType.BUSINESS_INVARIANT)
        );
        verify(collectInboxRepository).save(actual);
    }

    @Test
    void 최대_시도에_도달하면_FAILED와_RETRY_EXHAUSTED로_마킹된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = createProcessingInbox(3L, 3);

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new QueryTimeoutException("3차 실패"))
                .given(collectInboxClaimedExecutor)
                .execute(any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertAll(
                () -> assertThat(actual.getStatus()).isEqualTo(CollectInboxStatus.FAILED),
                () -> assertThat(actual.getFailureType()).isEqualTo(CollectInboxFailureType.RETRY_EXHAUSTED)
        );
        verify(collectInboxRepository).save(actual);
    }

    @Test
    void 긴_실패사유는_500자로_잘려서_저장된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = createProcessingInbox(4L, 1);

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new RuntimeException("x".repeat(600)))
                .given(collectInboxClaimedExecutor)
                .execute(any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertThat(actual.getFailureReason()).hasSize(500);
        verify(collectInboxRepository).save(actual);
    }

    @Test
    void 예외메시지가_비어있으면_unknown_failure로_저장된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = createProcessingInbox(5L, 1);

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new RuntimeException())
                .given(collectInboxClaimedExecutor)
                .execute(any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertThat(actual.getFailureReason()).isEqualTo("unknown failure");
        verify(collectInboxRepository).save(actual);
    }

    private CollectInbox createProcessingInbox(long runId, int processingAttempt) {
        CollectInbox inbox = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED, 1L, runId, "{}"
        );
        ReflectionTestUtils.setField(inbox, "status", CollectInboxStatus.PROCESSING);
        ReflectionTestUtils.setField(inbox, "processingAttempt", processingAttempt);
        return inbox;
    }
}
