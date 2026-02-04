package com.prism.statistics.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.async.pull-request-opened-derived-metrics")
public record PullRequestOpenedDerivedMetricsAsyncProperties(
        @DefaultValue("4") int corePoolSize,
        @DefaultValue("8") int maxPoolSize,
        @DefaultValue("100") int queueCapacity,
        @DefaultValue("60") int keepAliveSeconds,
        @DefaultValue("30") int awaitTerminationSeconds,
        @DefaultValue("true") boolean waitForTasksToCompleteOnShutdown,
        @DefaultValue("async-pr-metrics-") String threadNamePrefix
) {
}
