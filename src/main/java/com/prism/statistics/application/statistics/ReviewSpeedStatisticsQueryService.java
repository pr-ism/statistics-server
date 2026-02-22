package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.ReviewSpeedStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.MergeWaitTimeStatistics;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.ReviewCompletionStatistics;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.ReviewWaitTimeStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.setting.ProjectCoreTimeSetting;
import com.prism.statistics.domain.project.setting.repository.ProjectCoreTimeSettingRepository;
import com.prism.statistics.domain.project.setting.vo.CoreTime;
import com.prism.statistics.domain.statistics.repository.ReviewSpeedStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.ReviewSpeedStatisticsDto;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewSpeedStatisticsQueryService {

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
                .map(this::toResponse)
                .orElse(ReviewSpeedStatisticsResponse.empty());
    }

    private CoreTime getCoreTime(Long projectId) {
        return projectCoreTimeSettingRepository.findByProjectId(projectId)
                .map(ProjectCoreTimeSetting::getCoreTime)
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
        if (dto.reviewedCount() == 0) {
            return ReviewWaitTimeStatistics.empty();
        }

        double avgReviewWaitMinutes = (double) dto.totalReviewWaitMinutes() / dto.reviewedCount();
        double reviewWaitP50Minutes = calculatePercentile(dto.reviewWaitMinutesList(), 50);
        double reviewWaitP90Minutes = calculatePercentile(dto.reviewWaitMinutesList(), 90);

        return ReviewWaitTimeStatistics.of(avgReviewWaitMinutes, reviewWaitP50Minutes, reviewWaitP90Minutes);
    }

    private MergeWaitTimeStatistics buildMergeWaitTimeStatistics(ReviewSpeedStatisticsDto dto) {
        if (dto.mergedWithApprovalCount() == 0) {
            return MergeWaitTimeStatistics.empty();
        }

        double avgMergeWaitMinutes = (double) dto.totalMergeWaitMinutes() / dto.mergedWithApprovalCount();

        return MergeWaitTimeStatistics.of(avgMergeWaitMinutes, dto.mergedWithApprovalCount());
    }

    private ReviewCompletionStatistics buildReviewCompletionStatistics(ReviewSpeedStatisticsDto dto) {
        if (dto.reviewedCount() == 0) {
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
            return 0.0;
        }

        List<Long> sortedValues = values.stream().sorted().toList();
        int index = (int) Math.ceil(percentile / 100.0 * sortedValues.size()) - 1;
        index = Math.max(0, Math.min(index, sortedValues.size() - 1));

        return sortedValues.get(index).doubleValue();
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
