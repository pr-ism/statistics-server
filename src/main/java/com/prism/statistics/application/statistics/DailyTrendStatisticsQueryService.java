package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.DailyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse.DailyPrTrend;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse.TrendSummary;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.DailyTrendStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.DailyTrendStatisticsDto.DailyPrCountDto;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyTrendStatisticsQueryService {

    private final DailyTrendStatisticsRepository dailyTrendStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public DailyTrendStatisticsResponse findDailyTrendStatistics(
            Long userId,
            Long projectId,
            DailyTrendStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        return dailyTrendStatisticsRepository
                .findDailyTrendStatisticsByProjectId(projectId, request.startDate(), request.endDate())
                .map(dto -> toResponse(dto))
                .orElse(DailyTrendStatisticsResponse.empty());
    }

    private DailyTrendStatisticsResponse toResponse(DailyTrendStatisticsDto dto) {
        List<DailyPrTrend> dailyCreatedTrend = dto.dailyCreatedCounts().stream()
                .map(d -> DailyPrTrend.of(d.date(), d.count()))
                .toList();

        List<DailyPrTrend> dailyMergedTrend = dto.dailyMergedCounts().stream()
                .map(d -> DailyPrTrend.of(d.date(), d.count()))
                .toList();

        TrendSummary summary = buildSummary(dto.dailyCreatedCounts(), dto.dailyMergedCounts());

        return new DailyTrendStatisticsResponse(dailyCreatedTrend, dailyMergedTrend, summary);
    }

    private TrendSummary buildSummary(
            List<DailyPrCountDto> createdCounts,
            List<DailyPrCountDto> mergedCounts
    ) {
        long totalCreatedCount = createdCounts.stream()
                .mapToLong(item -> item.count())
                .sum();

        long totalMergedCount = mergedCounts.stream()
                .mapToLong(item -> item.count())
                .sum();

        int createdDays = createdCounts.size();
        int mergedDays = mergedCounts.size();

        double avgDailyCreatedCount = createdDays > 0 ? (double) totalCreatedCount / createdDays : 0.0;
        double avgDailyMergedCount = mergedDays > 0 ? (double) totalMergedCount / mergedDays : 0.0;

        Optional<DailyPrCountDto> peakCreated = createdCounts.stream()
                .max(Comparator.comparingLong(item -> item.count()));

        Optional<DailyPrCountDto> peakMerged = mergedCounts.stream()
                .max(Comparator.comparingLong(item -> item.count()));

        return TrendSummary.of(
                totalCreatedCount,
                totalMergedCount,
                avgDailyCreatedCount,
                avgDailyMergedCount,
                peakCreated.map(item -> item.date()).orElse(null),
                peakCreated.map(item -> item.count()).orElse(0L),
                peakMerged.map(item -> item.date()).orElse(null),
                peakMerged.map(item -> item.count()).orElse(0L)
        );
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
