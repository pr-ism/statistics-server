package com.prism.statistics.domain.metric.repository.dto;

import java.time.LocalDateTime;

public record TrendStatisticsDto(
        LocalDateTime githubCreatedAt,
        int additionCount,
        int deletionCount
) {
}
