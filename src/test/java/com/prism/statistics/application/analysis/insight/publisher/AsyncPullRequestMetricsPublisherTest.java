package com.prism.statistics.application.analysis.insight.publisher;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.insight.PullRequestMetricsEvent;
import com.prism.statistics.application.analysis.insight.PullRequestMetricsPublisher;
import com.prism.statistics.application.analysis.insight.PullRequestMetricsService;
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
        Long pullRequestId = 1L;

        doNothing().when(metricsService).deriveMetrics(pullRequestId);

        // when
        publisher.publish(new PullRequestMetricsEvent(pullRequestId));

        // then
        verify(metricsService, timeout(6000)).deriveMetrics(pullRequestId);
    }
}
