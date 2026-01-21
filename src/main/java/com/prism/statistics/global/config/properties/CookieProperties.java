package com.prism.statistics.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cookie")
public record CookieProperties(String domain, String path, String sameSite) {
}
