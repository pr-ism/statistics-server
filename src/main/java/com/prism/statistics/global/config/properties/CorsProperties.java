package com.prism.statistics.global.config.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedOriginPatterns,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        List<String> exposedHeaders,
        Long maxAge
) {

    public CorsProperties {
        allowedOrigins = (allowedOrigins == null || allowedOrigins.isEmpty())
                ? List.of("http://localhost:3000")
                : List.copyOf(allowedOrigins);

        allowedOriginPatterns = (allowedOriginPatterns == null)
                ? List.of()
                : List.copyOf(allowedOriginPatterns);

        allowedMethods = (allowedMethods == null || allowedMethods.isEmpty())
                ? List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                : List.copyOf(allowedMethods);

        allowedHeaders = (allowedHeaders == null || allowedHeaders.isEmpty())
                ? List.of(
                        "Authorization",
                        "Content-Type",
                        "Accept",
                        "Cache-Control"
                )
                : List.copyOf(allowedHeaders);

        exposedHeaders = (exposedHeaders == null)
                ? List.of()
                : List.copyOf(exposedHeaders);

        maxAge = (maxAge == null) ? 3600L : maxAge;
    }

    public boolean hasOriginPatterns() {
        return !allowedOriginPatterns.isEmpty();
    }

    public boolean hasExposedHeaders() {
        return !exposedHeaders.isEmpty();
    }
}
