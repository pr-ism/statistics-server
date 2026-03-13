package com.prism.statistics.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.collect.retry")
public record CollectRetryProperties(
        @DefaultValue("3") int maxAttempts
) {
}
