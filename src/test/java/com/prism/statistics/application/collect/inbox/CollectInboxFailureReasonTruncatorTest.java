package com.prism.statistics.application.collect.inbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CollectInboxFailureReasonTruncatorTest {

    private final CollectInboxFailureReasonTruncator truncator = new CollectInboxFailureReasonTruncator();

    @Test
    void null이면_null을_반환한다() {
        assertThat(truncator.truncate(null)).isNull();
    }

    @Test
    void _500자_이하면_그대로_반환한다() {
        String reason = "짧은 실패 사유";

        assertThat(truncator.truncate(reason)).isEqualTo(reason);
    }

    @Test
    void _500자_초과면_500자로_잘라서_반환한다() {
        String reason = "a".repeat(600);

        String result = truncator.truncate(reason);

        assertThat(result).hasSize(500);
    }
}
