package com.prism.statistics.application.analysis.insight.publisher;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.insight.PullRequestMetricsPublisher;
import com.prism.statistics.application.analysis.insight.PullRequestMetricsService;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AsyncPullRequestMetricsPublisherTest {

    @Autowired
    private PullRequestMetricsPublisher publisher;

    @Autowired
    private PullRequestMetricsService metricsService;

    @Test
    void publish_호출시_deriveMetrics를_비동기로_실행한다() {
        // given
        PullRequestOpenCreatedEvent event = createEvent();

        doNothing().when(metricsService).deriveMetrics(event);

        // when
        publisher.publish(event);

        // then
        verify(metricsService, timeout(6000)).deriveMetrics(event);
    }

    private PullRequestOpenCreatedEvent createEvent() {
        return new PullRequestOpenCreatedEvent(
                1L,
                2L,
                "abc123",
                PullRequestState.OPEN,
                PullRequestChangeStats.create(1, 2, 3),
                1,
                LocalDateTime.of(2024, 1, 15, 10, 0),
                List.of(),
                List.of()
        );
    }
}
