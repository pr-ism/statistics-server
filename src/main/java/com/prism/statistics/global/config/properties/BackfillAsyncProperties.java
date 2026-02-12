package com.prism.statistics.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.async.backfill")
public record BackfillAsyncProperties(
        @DefaultValue("5") int corePoolSize,
        @DefaultValue("5") int maxPoolSize,
        @DefaultValue("50") int queueCapacity,
        @DefaultValue("60") int keepAliveSeconds,
        @DefaultValue("30") int awaitTerminationSeconds,
        @DefaultValue("true") boolean waitForTasksToCompleteOnShutdown,
        @DefaultValue("backfill-") String threadNamePrefix
) {
}
