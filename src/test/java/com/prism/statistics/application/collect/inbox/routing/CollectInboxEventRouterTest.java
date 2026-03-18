package com.prism.statistics.application.collect.inbox.routing;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxEventRouterTest {

    @Mock
    CollectInboxEventHandler handler;

    @Mock
    CollectInboxEventHandler duplicateHandler;

    @Test
    void 등록된_collectType이면_해당_핸들러가_호출된다() {
        // given
        given(handler.supportType()).willReturn(CollectInboxType.PULL_REQUEST_OPENED);
        CollectInboxEventRouter router = new CollectInboxEventRouter(List.of(handler));
        CollectInboxContext context = new CollectInboxContext(1L, "{}");

        // when
        router.route(context, CollectInboxType.PULL_REQUEST_OPENED);

        // then
        verify(handler).handle(context);
    }

    @Test
    void 등록되지_않은_collectType이면_예외가_발생한다() {
        // given
        given(handler.supportType()).willReturn(CollectInboxType.PULL_REQUEST_OPENED);
        CollectInboxEventRouter router = new CollectInboxEventRouter(List.of(handler));
        CollectInboxContext context = new CollectInboxContext(1L, "{}");

        // when & then
        assertThatThrownBy(() -> router.route(context, CollectInboxType.PULL_REQUEST_CLOSED))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 동일한_collectType의_핸들러가_중복_등록되면_예외가_발생한다() {
        // given
        given(handler.supportType()).willReturn(CollectInboxType.PULL_REQUEST_OPENED);
        given(duplicateHandler.supportType()).willReturn(CollectInboxType.PULL_REQUEST_OPENED);

        // when & then
        assertThatThrownBy(() -> new CollectInboxEventRouter(List.of(handler, duplicateHandler)))
                .isInstanceOf(IllegalStateException.class);
    }
}
