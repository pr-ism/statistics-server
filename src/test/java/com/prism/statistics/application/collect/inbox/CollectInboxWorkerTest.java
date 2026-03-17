package com.prism.statistics.application.collect.inbox;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.prism.statistics.global.config.properties.CollectInboxProperties;
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
class CollectInboxWorkerTest {

    @Mock
    CollectInboxProcessor collectInboxProcessor;

    CollectInboxProperties collectInboxProperties;

    CollectInboxWorker collectInboxWorker;

    @BeforeEach
    void setUp() {
        collectInboxProperties = new CollectInboxProperties(200L, 60000L, true, 30);
        collectInboxWorker = new CollectInboxWorker(collectInboxProperties, collectInboxProcessor);
    }

    @Test
    void 워커는_배치_크기로_인박스를_처리한다() {
        // when
        collectInboxWorker.processInbox();

        // then
        verify(collectInboxProcessor).processPending(30);
    }

    @Test
    void 워커_실행_중_예외가_발생해도_예외를_전파하지_않는다() {
        // given
        willThrow(new RuntimeException("worker failure"))
                .given(collectInboxProcessor)
                .processPending(30);

        // when & then
        assertThatCode(() -> collectInboxWorker.processInbox())
                .doesNotThrowAnyException();
    }

    @Test
    void worker_enabled가_false면_인박스를_처리하지_않는다() {
        // given
        collectInboxProperties = new CollectInboxProperties(200L, 60000L, false, 30);
        collectInboxWorker = new CollectInboxWorker(collectInboxProperties, collectInboxProcessor);

        // when
        collectInboxWorker.processInbox();

        // then
        verify(collectInboxProcessor, never()).processPending(30);
    }

    @Test
    void 타임아웃_복구_워커는_타임아웃_복구를_실행한다() {
        // when
        collectInboxWorker.recoverTimeoutProcessing();

        // then
        verify(collectInboxProcessor).recoverTimeoutProcessing();
    }

    @Test
    void 타임아웃_복구_워커_실행_중_예외가_발생해도_예외를_전파하지_않는다() {
        // given
        willThrow(new RuntimeException("recovery failure"))
                .given(collectInboxProcessor)
                .recoverTimeoutProcessing();

        // when & then
        assertThatCode(() -> collectInboxWorker.recoverTimeoutProcessing())
                .doesNotThrowAnyException();
    }

    @Test
    void worker_enabled가_false면_타임아웃_복구를_실행하지_않는다() {
        // given
        collectInboxProperties = new CollectInboxProperties(200L, 60000L, false, 30);
        collectInboxWorker = new CollectInboxWorker(collectInboxProperties, collectInboxProcessor);

        // when
        collectInboxWorker.recoverTimeoutProcessing();

        // then
        verify(collectInboxProcessor, never()).recoverTimeoutProcessing();
    }
}
