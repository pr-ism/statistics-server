package com.prism.statistics.application.collect.inbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.prism.statistics.application.collect.inbox.routing.CollectInboxContext;
import com.prism.statistics.application.collect.inbox.routing.CollectInboxEventRouter;
import com.prism.statistics.infrastructure.collect.inbox.CollectInbox;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxStatus;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import com.prism.statistics.infrastructure.collect.inbox.repository.CollectInboxRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxClaimedExecutorTest {

    @Mock
    CollectInboxRepository collectInboxRepository;

    @Mock
    CollectInboxEventRouter collectInboxEventRouter;

    CollectInboxClaimedExecutor collectInboxClaimedExecutor;

    Clock fixedClock = Clock.fixed(Instant.parse("2026-03-16T00:00:00Z"), ZoneId.of("Asia/Seoul"));

    @BeforeEach
    void setUp() {
        collectInboxClaimedExecutor = new CollectInboxClaimedExecutor(
                fixedClock,
                collectInboxRepository,
                collectInboxEventRouter,
                new ProcessingSourceContext()
        );
    }

    @Test
    void 비즈니스_로직_실행_후_PROCESSED로_마킹하고_저장한다() {
        // given
        CollectInbox inbox = createProcessingInbox();

        // when
        collectInboxClaimedExecutor.execute(inbox);

        // then
        assertAll(
                () -> assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.PROCESSED),
                () -> assertThat(inbox.getProcessedAt()).isEqualTo(fixedClock.instant())
        );
        verify(collectInboxEventRouter).route(
                eq(new CollectInboxContext(1L, "{}")),
                eq(CollectInboxType.PULL_REQUEST_OPENED)
        );
        verify(collectInboxRepository).save(inbox);
    }

    @Test
    void 비즈니스_로직_실행_중_예외가_발생하면_상태를_변경하지_않고_예외를_전파한다() {
        // given
        CollectInbox inbox = createProcessingInbox();
        willThrow(new RuntimeException("비즈니스 로직 실패"))
                .given(collectInboxEventRouter)
                .route(
                        eq(new CollectInboxContext(1L, "{}")),
                        eq(CollectInboxType.PULL_REQUEST_OPENED)
                );

        // when & then
        assertThatThrownBy(() -> collectInboxClaimedExecutor.execute(inbox))
                .isInstanceOf(RuntimeException.class);

        assertThat(inbox.getStatus()).isEqualTo(CollectInboxStatus.PROCESSING);
        verify(collectInboxRepository, never()).save(inbox);
    }

    private CollectInbox createProcessingInbox() {
        CollectInbox inbox = CollectInbox.pending(
                CollectInboxType.PULL_REQUEST_OPENED, 1L, 1L, "{}"
        );
        ReflectionTestUtils.setField(inbox, "status", CollectInboxStatus.PROCESSING);
        return inbox;
    }
}
