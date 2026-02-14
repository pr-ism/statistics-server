package com.prism.statistics.application.metric;

import com.prism.statistics.application.metric.dto.request.TrendStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.TrendStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.TrendStatisticsResponse.TrendDataPoint;
import com.prism.statistics.domain.metric.TrendPeriod;
import com.prism.statistics.domain.metric.repository.TrendStatisticsRepository;
import com.prism.statistics.domain.metric.repository.dto.TrendStatisticsDto;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrendStatisticsQueryService {

    private final TrendStatisticsRepository trendStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public TrendStatisticsResponse findTrendStatistics(
            Long userId,
            Long projectId,
            TrendStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        List<TrendStatisticsDto> dtos = trendStatisticsRepository.findPullRequestsByProjectId(
                projectId, request.startDate(), request.endDate()
        );

        TrendPeriod period = request.period();

        Map<LocalDate, List<TrendStatisticsDto>> grouped = dtos.stream()
                .collect(Collectors.groupingBy(
                        dto -> period.periodStartOf(dto.githubCreatedAt().toLocalDate())
                ));

        List<LocalDate> allPeriods = period.generatePeriodStarts(request.startDate(), request.endDate());

        List<TrendDataPoint> trends = allPeriods.stream()
                .map(periodStart -> toDataPoint(periodStart, grouped))
                .toList();

        return new TrendStatisticsResponse(period.name(), trends);
    }

    private TrendDataPoint toDataPoint(LocalDate periodStart, Map<LocalDate, List<TrendStatisticsDto>> grouped) {
        List<TrendStatisticsDto> pullRequests = grouped.getOrDefault(periodStart, List.of());

        if (pullRequests.isEmpty()) {
            return TrendDataPoint.empty(periodStart);
        }

        double avgChange = pullRequests.stream()
                .mapToInt(dto -> dto.additionCount() + dto.deletionCount())
                .average()
                .orElse(0.0);

        return new TrendDataPoint(periodStart, pullRequests.size(), avgChange);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
