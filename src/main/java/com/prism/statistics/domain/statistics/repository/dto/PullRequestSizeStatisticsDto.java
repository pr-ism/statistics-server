package com.prism.statistics.domain.statistics.repository.dto;

import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record PullRequestSizeStatisticsDto(
        long totalCount,
        BigDecimal totalSizeScore,
        Map<SizeGrade, Long> sizeGradeDistribution,
        long largePullRequestCount,
        List<PullRequestSizeCorrelationDataDto> correlationData
) {

    public record PullRequestSizeCorrelationDataDto(
            Long pullRequestId,
            BigDecimal sizeScore,
            Long reviewWaitMinutes,
            Integer reviewRoundTrips
    ) {
    }
}
