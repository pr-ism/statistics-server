package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.ThroughputStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ThroughputStatisticsResponse;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.ThroughputStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ThroughputStatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThroughputStatisticsQueryService {

    private static final double PERCENT_MULTIPLIER = 100.0;
    private static final double ZERO_DOUBLE = 0.0;

    private final ThroughputStatisticsRepository throughputStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public ThroughputStatisticsResponse findThroughputStatistics(
            Long userId,
            Long projectId,
            ThroughputStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        return throughputStatisticsRepository
                .findThroughputStatisticsByProjectId(projectId, request.startDate(), request.endDate())
                .map(dto -> toResponse(dto))
                .orElse(ThroughputStatisticsResponse.empty());
    }

    private ThroughputStatisticsResponse toResponse(ThroughputStatisticsDto dto) {
        long mergedCount = dto.mergedCount();
        long closedCount = dto.closedCount();

        double avgMergeTimeMinutes = calculateAvgMergeTime(dto.totalMergeTimeMinutes(), mergedCount);
        double mergeSuccessRate = calculateMergeSuccessRate(mergedCount, closedCount);
        double closedPrRate = calculateClosedPrRate(mergedCount, closedCount);

        return ThroughputStatisticsResponse.of(
                mergedCount,
                closedCount,
                avgMergeTimeMinutes,
                mergeSuccessRate,
                closedPrRate
        );
    }

    private double calculateAvgMergeTime(long totalMergeTimeMinutes, long mergedCount) {
        if (mergedCount == 0L) {
            return ZERO_DOUBLE;
        }
        return (double) totalMergeTimeMinutes / mergedCount;
    }

    private double calculateMergeSuccessRate(long mergedCount, long closedCount) {
        long totalClosedPrs = mergedCount + closedCount;
        if (totalClosedPrs == 0L) {
            return ZERO_DOUBLE;
        }
        return (double) mergedCount / totalClosedPrs * PERCENT_MULTIPLIER;
    }

    private double calculateClosedPrRate(long mergedCount, long closedCount) {
        long totalClosedPrs = mergedCount + closedCount;
        if (totalClosedPrs == 0L) {
            return ZERO_DOUBLE;
        }
        return (double) closedCount / totalClosedPrs * PERCENT_MULTIPLIER;
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
