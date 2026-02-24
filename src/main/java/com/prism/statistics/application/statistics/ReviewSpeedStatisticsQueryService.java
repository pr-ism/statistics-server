package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.ReviewSpeedStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.MergeWaitTimeStatistics;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.ReviewCompletionStatistics;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.ReviewWaitTimeStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.setting.repository.ProjectCoreTimeSettingRepository;
import com.prism.statistics.domain.project.setting.vo.CoreTime;
import com.prism.statistics.domain.statistics.repository.ReviewSpeedStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSpeedStatisticsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewSpeedStatisticsQueryService {

    private static final long ZERO_COUNT = 0L;
    private static final double ZERO_DOUBLE = 0.0;
    private static final double PERCENT_SCALE = 100.0;
    private static final double PERCENT_ROUNDING_MULTIPLIER = 10000.0;
    private static final int PERCENTILE_P50 = 50;
    private static final int PERCENTILE_P90 = 90;

    private final ReviewSpeedStatisticsRepository reviewSpeedStatisticsRepository;
    private final ProjectCoreTimeSettingRepository projectCoreTimeSettingRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public ReviewSpeedStatisticsResponse findReviewSpeedStatistics(
            Long userId,
            Long projectId,
            ReviewSpeedStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        CoreTime coreTime = getCoreTime(projectId);

        return reviewSpeedStatisticsRepository
                .findReviewSpeedStatisticsByProjectId(
                        projectId,
                        request.startDate(),
                        request.endDate(),
                        coreTime.getStartTime(),
                        coreTime.getEndTime()
                )
                .map(dto -> toResponse(dto))
                .orElse(ReviewSpeedStatisticsResponse.empty());
    }

    private CoreTime getCoreTime(Long projectId) {
        return projectCoreTimeSettingRepository.findByProjectId(projectId)
                .map(setting -> setting.getCoreTime())
                .orElse(CoreTime.defaultCoreTime());
    }

    private ReviewSpeedStatisticsResponse toResponse(ReviewSpeedStatisticsDto dto) {
        long totalCount = dto.totalCount();
        long reviewedCount = dto.reviewedCount();
        double reviewRate = calculatePercentage(reviewedCount, totalCount);

        ReviewWaitTimeStatistics reviewWaitTime = buildReviewWaitTimeStatistics(dto);
        MergeWaitTimeStatistics mergeWaitTime = buildMergeWaitTimeStatistics(dto);
        ReviewCompletionStatistics reviewCompletion = buildReviewCompletionStatistics(dto);

        return new ReviewSpeedStatisticsResponse(
                totalCount,
                reviewedCount,
                reviewRate,
                reviewWaitTime,
                mergeWaitTime,
                reviewCompletion
        );
    }

    private ReviewWaitTimeStatistics buildReviewWaitTimeStatistics(ReviewSpeedStatisticsDto dto) {
        if (isReviewedCountEmpty(dto)) {
            return ReviewWaitTimeStatistics.empty();
        }

        double avgReviewWaitMinutes = calculateAverage(dto.totalReviewWaitMinutes(), dto.reviewedCount());
        double reviewWaitP50Minutes = calculatePercentile(dto.reviewWaitMinutesList(), PERCENTILE_P50);
        double reviewWaitP90Minutes = calculatePercentile(dto.reviewWaitMinutesList(), PERCENTILE_P90);

        return ReviewWaitTimeStatistics.of(avgReviewWaitMinutes, reviewWaitP50Minutes, reviewWaitP90Minutes);
    }

    private MergeWaitTimeStatistics buildMergeWaitTimeStatistics(ReviewSpeedStatisticsDto dto) {
        if (dto.mergedWithApprovalCount() == ZERO_COUNT) {
            return MergeWaitTimeStatistics.empty();
        }

        double avgMergeWaitMinutes = calculateAverage(dto.totalMergeWaitMinutes(), dto.mergedWithApprovalCount());

        return MergeWaitTimeStatistics.of(avgMergeWaitMinutes, dto.mergedWithApprovalCount());
    }

    private ReviewCompletionStatistics buildReviewCompletionStatistics(ReviewSpeedStatisticsDto dto) {
        if (isReviewedCountEmpty(dto)) {
            return ReviewCompletionStatistics.empty();
        }

        double coreTimeReviewRate = calculatePercentage(dto.coreTimeReviewCount(), dto.reviewedCount());
        double sameDayReviewRate = calculatePercentage(dto.sameDayReviewCount(), dto.reviewedCount());

        return ReviewCompletionStatistics.of(
                coreTimeReviewRate,
                dto.coreTimeReviewCount(),
                sameDayReviewRate,
                dto.sameDayReviewCount()
        );
    }

    private double calculatePercentile(List<Long> values, int percentile) {
        if (values == null || values.isEmpty()) {
            return ZERO_DOUBLE;
        }

        List<Long> sortedValues = values.stream().sorted().toList();
        double percentileFraction = percentile / PERCENT_SCALE;
        int index = (int) Math.ceil(percentileFraction * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));

        return sortedValues.get(index).doubleValue();
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }

    private double calculatePercentage(long count, long totalCount) {
        if (totalCount == ZERO_COUNT) {
            return ZERO_DOUBLE;
        }
        return Math.round(count * PERCENT_ROUNDING_MULTIPLIER / totalCount) / PERCENT_SCALE;
    }

    private double calculateAverage(long total, long count) {
        if (count == ZERO_COUNT) {
            return ZERO_DOUBLE;
        }
        return (double) total / count;
    }

    private boolean isReviewedCountEmpty(ReviewSpeedStatisticsDto dto) {
        return dto.reviewedCount() == ZERO_COUNT;
    }
}
