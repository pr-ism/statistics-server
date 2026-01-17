package com.prism.statistics.global.config.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.user.nickname")
public record NicknameProperties(
        List<String> adjective,
        List<String> color
) {
}
