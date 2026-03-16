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

import com.prism.statistics.application.collect.inbox.routing.CollectInboxContext;
import com.prism.statistics.application.collect.inbox.routing.CollectInboxEventRouter;
import com.prism.statistics.global.config.properties.CollectRetryProperties;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxFailureType;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxStatus;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.infrastructure.collect.inbox.repository.CollectInboxRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.dao.QueryTimeoutException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxEntryProcessorTest {

    @Mock
    CollectInboxRepository collectInboxRepository;

    @Mock
    CollectInboxEventRouter collectInboxServiceRouter;

    CollectInboxEntryProcessor collectInboxEntryProcessor;

    @BeforeEach
    void setUp() {
        CollectRetryProperties retryProperties = new CollectRetryProperties(3);

        collectInboxEntryProcessor = new CollectInboxEntryProcessor(
                Clock.systemUTC(),
                retryProperties,
                collectInboxRepository,
                collectInboxServiceRouter,
                new CollectInboxFailureReasonTruncator(),
                new ProcessingSourceContext(),
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
        verify(collectInboxRepository, never()).save(any());
        verify(collectInboxServiceRouter, never()).route(any(), any());
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
        verify(collectInboxRepository, never()).save(any());
        verify(collectInboxServiceRouter, never()).route(any(), any());
    }

    @Test
    void 정상처리시_ServiceRouter가_호출되고_PROCESSED로_저장된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(11L);

        CollectInbox actual = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED,
                1L,
                1L,
                "{}"
        );
        actual.markProcessing(Instant.parse("2026-02-15T00:00:00Z"));

        given(collectInboxRepository.markProcessingIfClaimable(eq(11L), any())).willReturn(true);
        given(collectInboxRepository.findById(11L)).willReturn(Optional.of(actual));

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertAll(
                () -> assertThat(actual.getStatus()).isEqualTo(CollectInboxStatus.PROCESSED),
                () -> assertThat(actual.getProcessingAttempt()).isEqualTo(1)
        );
        verify(collectInboxServiceRouter).route(
                eq(new CollectInboxContext(1L, "{}")),
                eq(CollectInboxType.PULL_REQUEST_OPENED)
        );
        verify(collectInboxRepository).save(actual);
    }

    @Test
    void 재시도_가능한_예외_발생시_첫_시도면_RETRY_PENDING으로_마킹된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED,
                1L,
                2L,
                "{}"
        );
        actual.markProcessing(Instant.parse("2026-02-15T00:00:00Z"));

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new QueryTimeoutException("DB 타임아웃"))
                .given(collectInboxServiceRouter)
                .route(any(), any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertAll(
                () -> assertThat(actual.getStatus()).isEqualTo(CollectInboxStatus.RETRY_PENDING),
                () -> assertThat(actual.getProcessingAttempt()).isEqualTo(1),
                () -> assertThat(actual.getFailureType()).isNull()
        );
        verify(collectInboxRepository).save(actual);
    }

    @Test
    void 재시도_불가능한_예외_발생시_즉시_FAILED와_BUSINESS_INVARIANT로_마킹된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED,
                1L,
                20L,
                "{}"
        );
        actual.markProcessing(Instant.parse("2026-02-15T00:00:00Z"));

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new IllegalArgumentException("비즈니스 로직 위반"))
                .given(collectInboxServiceRouter)
                .route(any(), any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertAll(
                () -> assertThat(actual.getStatus()).isEqualTo(CollectInboxStatus.FAILED),
                () -> assertThat(actual.getProcessingAttempt()).isEqualTo(1),
                () -> assertThat(actual.getFailureType()).isEqualTo(CollectInboxFailureType.BUSINESS_INVARIANT)
        );
        verify(collectInboxRepository).save(actual);
    }

    @Test
    void 최대_시도에_도달하면_FAILED와_RETRY_EXHAUSTED로_마킹된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED,
                1L,
                3L,
                "{}"
        );
        actual.markProcessing(Instant.parse("2026-02-15T00:00:00Z"));
        actual.markRetryPending(Instant.parse("2026-02-15T00:01:00Z"), "1차 실패");
        actual.markProcessing(Instant.parse("2026-02-15T00:02:00Z"));
        actual.markRetryPending(Instant.parse("2026-02-15T00:03:00Z"), "2차 실패");
        actual.markProcessing(Instant.parse("2026-02-15T00:04:00Z"));

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new QueryTimeoutException("3차 실패"))
                .given(collectInboxServiceRouter)
                .route(any(), any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertAll(
                () -> assertThat(actual.getStatus()).isEqualTo(CollectInboxStatus.FAILED),
                () -> assertThat(actual.getProcessingAttempt()).isEqualTo(3),
                () -> assertThat(actual.getFailureType()).isEqualTo(CollectInboxFailureType.RETRY_EXHAUSTED)
        );
        verify(collectInboxRepository).save(actual);
    }

    @Test
    void 긴_실패사유는_500자로_잘려서_저장된다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(pending.getId()).willReturn(10L);

        CollectInbox actual = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED,
                1L,
                4L,
                "{}"
        );
        actual.markProcessing(Instant.parse("2026-02-15T00:00:00Z"));

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new RuntimeException("x".repeat(600)))
                .given(collectInboxServiceRouter)
                .route(any(), any());

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

        CollectInbox actual = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED,
                1L,
                5L,
                "{}"
        );
        actual.markProcessing(Instant.parse("2026-02-15T00:00:00Z"));

        given(collectInboxRepository.markProcessingIfClaimable(eq(10L), any())).willReturn(true);
        given(collectInboxRepository.findById(10L)).willReturn(Optional.of(actual));
        willThrow(new RuntimeException())
                .given(collectInboxServiceRouter)
                .route(any(), any());

        // when
        collectInboxEntryProcessor.process(pending);

        // then
        assertThat(actual.getFailureReason()).isEqualTo("unknown failure");
        verify(collectInboxRepository).save(actual);
    }
}
