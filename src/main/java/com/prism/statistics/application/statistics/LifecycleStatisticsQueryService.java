package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.LifecycleStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.LifecycleStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.LifecycleStatisticsResponse.AverageTimeStatistics;
import com.prism.statistics.application.statistics.dto.response.LifecycleStatisticsResponse.HealthStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.LifecycleStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.LifecycleStatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LifecycleStatisticsQueryService {

    private final LifecycleStatisticsRepository lifecycleStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public LifecycleStatisticsResponse findLifecycleStatistics(
            Long userId,
            Long projectId,
            LifecycleStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        return lifecycleStatisticsRepository
                .findLifecycleStatisticsByProjectId(projectId, request.startDate(), request.endDate())
                .map(stats -> toResponse(stats))
                .orElse(LifecycleStatisticsResponse.empty());
    }

    private LifecycleStatisticsResponse toResponse(LifecycleStatisticsDto dto) {
        long totalCount = dto.totalCount();
        long mergedCount = dto.mergedCount();
        long closedWithoutMergeCount = dto.closedWithoutMergeCount();

        double mergeRate = calculatePercentage(mergedCount, totalCount);

        AverageTimeStatistics averageTime = calculateAverageTime(dto);
        HealthStatistics health = calculateHealth(dto);

        return new LifecycleStatisticsResponse(
                totalCount,
                mergedCount,
                closedWithoutMergeCount,
                mergeRate,
                averageTime,
                health
        );
    }

    private AverageTimeStatistics calculateAverageTime(LifecycleStatisticsDto dto) {
        long totalCount = dto.totalCount();

        if (totalCount == 0L) {
            return AverageTimeStatistics.empty();
        }

        long avgTimeToMerge = calculateAvgTimeToMerge(dto);
        long avgLifespan = calculateAvgLifespan(dto);
        long avgActiveWork = calculateAvgActiveWork(dto);

        return AverageTimeStatistics.of(avgTimeToMerge, avgLifespan, avgActiveWork);
    }

    private long calculateAvgTimeToMerge(LifecycleStatisticsDto dto) {
        long mergedCount = dto.mergedCount();

        if (mergedCount == 0L) {
            return 0L;
        }

        return dto.totalTimeToMergeMinutes() / mergedCount;
    }

    private long calculateAvgLifespan(LifecycleStatisticsDto dto) {
        long closedCount = dto.mergedCount() + dto.closedWithoutMergeCount();

        if (closedCount == 0L) {
            return 0L;
        }

        return dto.totalLifespanMinutes() / closedCount;
    }

    private long calculateAvgActiveWork(LifecycleStatisticsDto dto) {
        long activeWorkCount = dto.activeWorkCount();

        if (activeWorkCount == 0L) {
            return 0L;
        }

        return dto.totalActiveWorkMinutes() / activeWorkCount;
    }

    private HealthStatistics calculateHealth(LifecycleStatisticsDto dto) {
        long totalCount = dto.totalCount();

        if (totalCount == 0L) {
            return HealthStatistics.empty();
        }

        double closedWithoutReviewRate = calculatePercentage(dto.closedWithoutReviewCount(), totalCount);
        double reopenedRate = calculatePercentage(dto.reopenedCount(), totalCount);
        double avgStateChangeCount = (double) dto.totalStateChangeCount() / totalCount;

        return HealthStatistics.of(
                dto.closedWithoutReviewCount(),
                closedWithoutReviewRate,
                dto.reopenedCount(),
                reopenedRate,
                Math.round(avgStateChangeCount * 100.0) / 100.0
        );
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }

    private double calculatePercentage(long count, long totalCount) {
        if (totalCount == 0L) {
            return 0.0;
        }
        return Math.round(count * 10000.0 / totalCount) / 100.0;
    }
}
