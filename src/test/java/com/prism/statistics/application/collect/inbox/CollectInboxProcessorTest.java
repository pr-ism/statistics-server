package com.prism.statistics.application.collect.inbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.prism.statistics.global.config.properties.CollectInboxProperties;
import com.prism.statistics.global.config.properties.CollectRetryProperties;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.infrastructure.collect.inbox.repository.CollectInboxRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxProcessorTest {

    @Mock
    CollectInboxRepository collectInboxRepository;

    @Mock
    CollectInboxEntryProcessor collectInboxEntryProcessor;

    CollectInboxProcessor collectInboxProcessor;

    Clock fixedClock = Clock.fixed(Instant.parse("2026-03-16T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @BeforeEach
    void setUp() {
        CollectInboxProperties inboxProperties = new CollectInboxProperties(200L, 60000L, true, 30);
        CollectRetryProperties retryProperties = new CollectRetryProperties(3);

        collectInboxProcessor = new CollectInboxProcessor(
                fixedClock,
                inboxProperties,
                retryProperties,
                collectInboxRepository,
                collectInboxEntryProcessor
        );
    }

    @Test
    void enqueue는_레포지토리에_위임한다() {
        // given
        given(collectInboxRepository.enqueue(any(), any(), anyLong(), anyString())).willReturn(true);

        // when
        boolean result = collectInboxProcessor.enqueue(
                CollectInboxType.PULL_REQUEST_OPENED, 1L, 123L, "{}"
        );

        // then
        assertThat(result).isTrue();
        verify(collectInboxRepository).enqueue(
                CollectInboxType.PULL_REQUEST_OPENED, 1L, 123L, "{}"
        );
    }

    @Test
    void processPending은_대기중인_엔트리를_처리한다() {
        // given
        CollectInbox pending = org.mockito.Mockito.mock(CollectInbox.class);
        given(collectInboxRepository.findClaimable(10)).willReturn(List.of(pending));

        // when
        collectInboxProcessor.processPending(10);

        // then
        verify(collectInboxEntryProcessor).process(pending);
    }

    @Test
    void processPending에서_대기중인_엔트리가_없으면_처리하지_않는다() {
        // given
        given(collectInboxRepository.findClaimable(10)).willReturn(List.of());

        // when
        collectInboxProcessor.processPending(10);

        // then
        verify(collectInboxEntryProcessor, never()).process(any());
    }

    @Test
    void 엔트리_처리_중_예외가_발생해도_다음_엔트리를_계속_처리한다() {
        // given
        CollectInbox first = org.mockito.Mockito.mock(CollectInbox.class);
        CollectInbox second = org.mockito.Mockito.mock(CollectInbox.class);
        given(collectInboxRepository.findClaimable(10)).willReturn(List.of(first, second));
        willThrow(new RuntimeException("처리 실패"))
                .given(collectInboxEntryProcessor).process(first);

        // when
        assertThatCode(() -> collectInboxProcessor.processPending(10))
                .doesNotThrowAnyException();

        // then
        verify(collectInboxEntryProcessor).process(first);
        verify(collectInboxEntryProcessor).process(second);
    }

    @Test
    void recoverTimeoutProcessing은_타임아웃된_엔트리를_복구한다() {
        // given
        given(collectInboxRepository.recoverTimeoutProcessing(any(), any(), anyString(), anyInt()))
                .willReturn(2);

        // when
        collectInboxProcessor.recoverTimeoutProcessing();

        // then
        verify(collectInboxRepository).recoverTimeoutProcessing(
                any(), any(), anyString(), eq(3)
        );
    }
}
