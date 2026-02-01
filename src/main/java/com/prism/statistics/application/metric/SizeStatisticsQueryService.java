package com.prism.statistics.application.metric;

import com.prism.statistics.application.metric.dto.request.SizeStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.SizeStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.SizeStatisticsResponse.SizeStatistics;
import com.prism.statistics.domain.metric.PullRequestSizeCategory;
import com.prism.statistics.domain.metric.repository.SizeStatisticsRepository;
import com.prism.statistics.domain.metric.repository.dto.SizeStatisticsDto;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SizeStatisticsQueryService {

    private final SizeStatisticsRepository sizeStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public SizeStatisticsResponse findSizeStatistics(Long userId, Long projectId, SizeStatisticsRequest request) {
        validateProjectOwnership(projectId, userId);

        List<SizeStatisticsDto> dtos = sizeStatisticsRepository
                .findSizeStatisticsByProjectId(projectId, request.startDate(), request.endDate());

        Map<String, SizeStatisticsDto> dtoMap = dtos.stream()
                .collect(Collectors.toMap(dto -> dto.sizeCategory(), dto -> dto));

        long totalCount = dtos.stream()
                .mapToLong(dto -> dto.count())
                .sum();

        List<SizeStatistics> sizeStatistics = Arrays.stream(PullRequestSizeCategory.values())
                .map(category -> toSizeStatistics(category, dtoMap, totalCount))
                .toList();

        return new SizeStatisticsResponse(sizeStatistics);
    }

    private SizeStatistics toSizeStatistics(
            PullRequestSizeCategory category,
            Map<String, SizeStatisticsDto> dtoMap,
            long totalCount
    ) {
        SizeStatisticsDto dto = dtoMap.get(category.name());

        if (dto == null) {
            return SizeStatistics.empty(category.name());
        }

        double percentage = calculatePercentage(dto.count(), totalCount);

        return SizeStatistics.of(
                category.name(),
                dto.count(),
                percentage,
                dto.averageChangedFileCount(),
                dto.averageCommitCount()
        );
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }

    private double calculatePercentage(long count, long totalCount) {
        return Math.round(count * 10000.0 / totalCount) / 100.0;
    }
}
