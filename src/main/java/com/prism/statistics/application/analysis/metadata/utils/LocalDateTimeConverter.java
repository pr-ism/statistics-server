package com.prism.statistics.application.analysis.metadata.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class LocalDateTimeConverter {

    private final Clock clock;

    public LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, clock.getZone());
    }
}
