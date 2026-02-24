package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.PullRequestSizeStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.PullRequestSizeStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.PullRequestSizeStatisticsResponse.CorrelationStatistics;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.PullRequestSizeStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.PullRequestSizeStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.PullRequestSizeStatisticsDto.PullRequestSizeCorrelationDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PullRequestSizeStatisticsQueryService {

    private static final double ZERO_DOUBLE = 0.0d;
    private static final long ZERO_COUNT = 0L;
    private static final int AVG_SIZE_SCORE_SCALE = 2;
    private static final RoundingMode AVG_SIZE_SCORE_ROUNDING = RoundingMode.HALF_UP;
    private static final int MIN_CORRELATION_SAMPLE_SIZE = 3;
    private static final int MIN_CORRELATION_DENOMINATOR = 2;
    private static final double CORRELATION_EPSILON = 1e-10;
    private static final double MIN_CORRELATION = -1.0d;
    private static final double MAX_CORRELATION = 1.0d;
    private static final double PERCENT_SCALE = 100.0d;
    private static final double PERCENT_ROUNDING_MULTIPLIER = 10000.0d;

    private final PullRequestSizeStatisticsRepository pullRequestSizeStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public PullRequestSizeStatisticsResponse findPullRequestSizeStatistics(
            Long userId,
            Long projectId,
            PullRequestSizeStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        return pullRequestSizeStatisticsRepository
                .findPullRequestSizeStatisticsByProjectId(projectId, request.startDate(), request.endDate())
                .map(dto -> toResponse(dto))
                .orElse(PullRequestSizeStatisticsResponse.empty());
    }

    private PullRequestSizeStatisticsResponse toResponse(PullRequestSizeStatisticsDto dto) {
        long totalCount = dto.totalCount();

        double avgSizeScore = calculateAvgSizeScore(dto.totalSizeScore(), totalCount);
        Map<SizeGrade, Long> sizeGradeDistribution = ensureAllGrades(dto.sizeGradeDistribution());
        double largePullRequestRate = calculatePercentage(dto.largePullRequestCount(), totalCount);

        CorrelationStatistics sizeReviewWaitCorrelation = calculateSizeReviewWaitCorrelation(dto.correlationData());
        CorrelationStatistics sizeReviewRoundTripCorrelation = calculateSizeReviewRoundTripCorrelation(dto.correlationData());

        return new PullRequestSizeStatisticsResponse(
                totalCount,
                avgSizeScore,
                sizeGradeDistribution,
                largePullRequestRate,
                sizeReviewWaitCorrelation,
                sizeReviewRoundTripCorrelation
        );
    }

    private double calculateAvgSizeScore(BigDecimal totalSizeScore, long totalCount) {
        if (totalCount == ZERO_COUNT || totalSizeScore == null) {
            return ZERO_DOUBLE;
        }

        return totalSizeScore
                .divide(BigDecimal.valueOf(totalCount), AVG_SIZE_SCORE_SCALE, AVG_SIZE_SCORE_ROUNDING)
                .doubleValue();
    }

    private Map<SizeGrade, Long> ensureAllGrades(Map<SizeGrade, Long> distribution) {
        Map<SizeGrade, Long> result = new EnumMap<>(SizeGrade.class);
        for (SizeGrade grade : SizeGrade.values()) {
            result.put(grade, distribution.getOrDefault(grade, 0L));
        }
        return result;
    }

    private CorrelationStatistics calculateSizeReviewWaitCorrelation(List<PullRequestSizeCorrelationDataDto> data) {
        List<double[]> validPairs = extractSizeReviewWaitPairs(data);

        if (isInsufficientCorrelationSamples(validPairs)) {
            return CorrelationStatistics.empty();
        }

        double[] x = validPairs.stream().mapToDouble(p -> p[0]).toArray();
        double[] y = validPairs.stream().mapToDouble(p -> p[1]).toArray();

        double correlation = calculatePearsonCorrelation(x, y);
        return CorrelationStatistics.of(correlation);
    }

    private CorrelationStatistics calculateSizeReviewRoundTripCorrelation(List<PullRequestSizeCorrelationDataDto> data) {
        List<double[]> validPairs = extractSizeReviewRoundTripPairs(data);

        if (isInsufficientCorrelationSamples(validPairs)) {
            return CorrelationStatistics.empty();
        }

        double[] x = validPairs.stream().mapToDouble(p -> p[0]).toArray();
        double[] y = validPairs.stream().mapToDouble(p -> p[1]).toArray();

        double correlation = calculatePearsonCorrelation(x, y);
        return CorrelationStatistics.of(correlation);
    }

    private double calculatePearsonCorrelation(double[] x, double[] y) {
        int n = x.length;
        if (n != y.length || n < MIN_CORRELATION_DENOMINATOR) {
            return Double.NaN;
        }

        double sumX = ZERO_DOUBLE;
        double sumY = ZERO_DOUBLE;
        double sumXY = ZERO_DOUBLE;
        double sumX2 = ZERO_DOUBLE;
        double sumY2 = ZERO_DOUBLE;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        if (Math.abs(denominator) < CORRELATION_EPSILON) {
            return Double.NaN;
        }

        double result = numerator / denominator;
        return Math.max(MIN_CORRELATION, Math.min(MAX_CORRELATION, result));
    }

    private List<double[]> extractSizeReviewWaitPairs(List<PullRequestSizeCorrelationDataDto> data) {
        return data.stream()
                .filter(d -> d.sizeScore() != null && d.reviewWaitMinutes() != null)
                .map(d -> new double[]{d.sizeScore().doubleValue(), d.reviewWaitMinutes().doubleValue()})
                .toList();
    }

    private List<double[]> extractSizeReviewRoundTripPairs(List<PullRequestSizeCorrelationDataDto> data) {
        return data.stream()
                .filter(d -> d.sizeScore() != null && d.reviewRoundTrips() != null)
                .map(d -> new double[]{d.sizeScore().doubleValue(), d.reviewRoundTrips().doubleValue()})
                .toList();
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

    private boolean isInsufficientCorrelationSamples(List<double[]> pairs) {
        return pairs.size() < MIN_CORRELATION_SAMPLE_SIZE;
    }
}
