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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DailyTrendStatisticsQueryService {

    private static final double ZERO_DOUBLE = 0.0;

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
                .map(dto -> toResponse(dto, request.startDate(), request.endDate()))
                .orElse(DailyTrendStatisticsResponse.empty());
    }

    private DailyTrendStatisticsResponse toResponse(
            DailyTrendStatisticsDto dto,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        List<DailyPrTrend> dailyCreatedTrend = dto.dailyCreatedCounts().stream()
                .map(d -> DailyPrTrend.of(d.date(), d.count()))
                .toList();

        List<DailyPrTrend> dailyMergedTrend = dto.dailyMergedCounts().stream()
                .map(d -> DailyPrTrend.of(d.date(), d.count()))
                .toList();

        TrendSummary summary = buildSummary(dto.dailyCreatedCounts(), dto.dailyMergedCounts(), startDate, endDate);

        return new DailyTrendStatisticsResponse(dailyCreatedTrend, dailyMergedTrend, summary);
    }

    private TrendSummary buildSummary(
            List<DailyPrCountDto> createdCounts,
            List<DailyPrCountDto> mergedCounts,
            java.time.LocalDate startDate,
            java.time.LocalDate endDate
    ) {
        long totalCreatedCount = createdCounts.stream()
                .mapToLong(item -> item.count())
                .sum();

        long totalMergedCount = mergedCounts.stream()
                .mapToLong(item -> item.count())
                .sum();

        long dateRangeDays = calculateDateRangeDays(startDate, endDate, createdCounts, mergedCounts);

        double avgDailyCreatedCount = calculateAverage(totalCreatedCount, dateRangeDays);
        double avgDailyMergedCount = calculateAverage(totalMergedCount, dateRangeDays);

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

    private long calculateDateRangeDays(
            java.time.LocalDate startDate,
            java.time.LocalDate endDate,
            List<DailyPrCountDto> createdCounts,
            List<DailyPrCountDto> mergedCounts
    ) {
        java.time.LocalDate resolvedStart = startDate;
        java.time.LocalDate resolvedEnd = endDate;

        if (resolvedStart == null || resolvedEnd == null) {
            java.time.LocalDate minDate = resolveMinDate(createdCounts, mergedCounts);
            java.time.LocalDate maxDate = resolveMaxDate(createdCounts, mergedCounts);

            if (resolvedStart == null) {
                resolvedStart = minDate;
            }
            if (resolvedEnd == null) {
                resolvedEnd = maxDate;
            }
        }

        if (resolvedStart == null || resolvedEnd == null) {
            return 0L;
        }
        if (resolvedEnd.isBefore(resolvedStart)) {
            return 0L;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(resolvedStart, resolvedEnd) + 1L;
    }

    private double calculateAverage(long total, long days) {
        if (days <= 0L) {
            return ZERO_DOUBLE;
        }
        return (double) total / days;
    }

    private java.time.LocalDate resolveMinDate(
            List<DailyPrCountDto> createdCounts,
            List<DailyPrCountDto> mergedCounts
    ) {
        return java.util.stream.Stream.concat(createdCounts.stream(), mergedCounts.stream())
                .map(item -> item.date())
                .min(java.time.LocalDate::compareTo)
                .orElse(null);
    }

    private java.time.LocalDate resolveMaxDate(
            List<DailyPrCountDto> createdCounts,
            List<DailyPrCountDto> mergedCounts
    ) {
        return java.util.stream.Stream.concat(createdCounts.stream(), mergedCounts.stream())
                .map(item -> item.date())
                .max(java.time.LocalDate::compareTo)
                .orElse(null);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
