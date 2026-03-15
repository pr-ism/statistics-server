package com.prism.statistics.application.collect.inbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProcessingSourceContextTest {

    ProcessingSourceContext context = new ProcessingSourceContext();

    @Test
    void 기본_상태는_inbox_processing이_아니다() {
        assertThat(context.isInboxProcessing()).isFalse();
    }

    @Test
    void withInboxProcessing_내부에서는_inbox_processing으로_인식한다() {
        // when
        context.withInboxProcessing(
                () -> assertThat(context.isInboxProcessing()).isTrue()
        );

        // then
        assertThat(context.isInboxProcessing()).isFalse();
    }

    @Test
    void withInboxProcessing_내부에서_예외가_발생해도_컨텍스트는_복원된다() {
        // given
        Runnable throwingAction = () -> { throw new RuntimeException("test"); };

        // when
        assertThatThrownBy(() -> context.withInboxProcessing(throwingAction))
                .isInstanceOf(RuntimeException.class);

        // then
        assertThat(context.isInboxProcessing()).isFalse();
    }
}
