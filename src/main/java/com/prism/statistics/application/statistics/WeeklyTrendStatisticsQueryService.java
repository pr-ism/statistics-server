package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.WeeklyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse.MonthlyThroughput;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse.WeeklyPrSize;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse.WeeklyReviewWaitTime;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse.WeeklyThroughput;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.WeeklyTrendStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.WeeklyTrendStatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeeklyTrendStatisticsQueryService {

    private final WeeklyTrendStatisticsRepository weeklyTrendStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public WeeklyTrendStatisticsResponse findWeeklyTrendStatistics(
            Long userId,
            Long projectId,
            WeeklyTrendStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        return weeklyTrendStatisticsRepository
                .findWeeklyTrendStatisticsByProjectId(projectId, request.startDate(), request.endDate())
                .map(dto -> toResponse(dto))
                .orElse(WeeklyTrendStatisticsResponse.empty());
    }

    private WeeklyTrendStatisticsResponse toResponse(WeeklyTrendStatisticsDto dto) {
        List<WeeklyThroughput> weeklyThroughput = dto.weeklyThroughputs().stream()
                .map(w -> WeeklyThroughput.of(w.weekStartDate(), w.mergedCount(), w.closedCount()))
                .toList();

        List<MonthlyThroughput> monthlyThroughput = dto.monthlyThroughputs().stream()
                .map(m -> MonthlyThroughput.of(m.year(), m.month(), m.mergedCount(), m.closedCount()))
                .toList();

        List<WeeklyReviewWaitTime> weeklyReviewWaitTimeTrend = dto.weeklyReviewWaitTimes().stream()
                .map(w -> WeeklyReviewWaitTime.of(w.weekStartDate(), w.avgReviewWaitTimeMinutes()))
                .toList();

        List<WeeklyPrSize> weeklyPrSizeTrend = dto.weeklyPrSizes().stream()
                .map(w -> WeeklyPrSize.of(w.weekStartDate(), w.avgSizeScore()))
                .toList();

        return new WeeklyTrendStatisticsResponse(
                weeklyThroughput,
                monthlyThroughput,
                weeklyReviewWaitTimeTrend,
                weeklyPrSizeTrend
        );
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
