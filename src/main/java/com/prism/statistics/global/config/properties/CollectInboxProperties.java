package com.prism.statistics.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.collect.inbox")
public record CollectInboxProperties(
        @DefaultValue("200") long pollDelayMs,
        @DefaultValue("60000") long processingTimeoutMs,
        @DefaultValue("true") boolean workerEnabled,
        @DefaultValue("30") int batchSize
) {
}
