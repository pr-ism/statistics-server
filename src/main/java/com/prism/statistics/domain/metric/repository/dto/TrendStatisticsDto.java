package com.prism.statistics.domain.metric.repository.dto;

import java.time.LocalDateTime;

public record TrendStatisticsDto(
        LocalDateTime pullRequestCreatedAt,
        int additionCount,
        int deletionCount
) {
}
