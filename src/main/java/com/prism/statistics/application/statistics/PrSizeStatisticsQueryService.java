package com.prism.statistics.application.statistics;

import com.prism.statistics.application.statistics.dto.request.PrSizeStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.PrSizeStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.PrSizeStatisticsResponse.CorrelationStatistics;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.statistics.repository.PrSizeStatisticsRepository;
import com.prism.statistics.domain.statistics.repository.dto.PrSizeStatisticsDto;
import com.prism.statistics.domain.statistics.repository.dto.PrSizeStatisticsDto.PrSizeCorrelationDataDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PrSizeStatisticsQueryService {

    private final PrSizeStatisticsRepository prSizeStatisticsRepository;
    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public PrSizeStatisticsResponse findPrSizeStatistics(
            Long userId,
            Long projectId,
            PrSizeStatisticsRequest request
    ) {
        validateProjectOwnership(projectId, userId);

        return prSizeStatisticsRepository
                .findPrSizeStatisticsByProjectId(projectId, request.startDate(), request.endDate())
                .map(dto -> toResponse(dto))
                .orElse(PrSizeStatisticsResponse.empty());
    }

    private PrSizeStatisticsResponse toResponse(PrSizeStatisticsDto dto) {
        long totalCount = dto.totalCount();

        double avgSizeScore = calculateAvgSizeScore(dto.totalSizeScore(), totalCount);
        Map<SizeGrade, Long> sizeGradeDistribution = ensureAllGrades(dto.sizeGradeDistribution());
        double largePrRate = calculatePercentage(dto.largePrCount(), totalCount);

        CorrelationStatistics sizeReviewWaitCorrelation = calculateSizeReviewWaitCorrelation(dto.correlationData());
        CorrelationStatistics sizeReviewRoundTripCorrelation = calculateSizeReviewRoundTripCorrelation(dto.correlationData());

        return new PrSizeStatisticsResponse(
                totalCount,
                avgSizeScore,
                sizeGradeDistribution,
                largePrRate,
                sizeReviewWaitCorrelation,
                sizeReviewRoundTripCorrelation
        );
    }

    private double calculateAvgSizeScore(BigDecimal totalSizeScore, long totalCount) {
        if (totalCount == 0 || totalSizeScore == null) {
            return 0.0;
        }

        return totalSizeScore
                .divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private Map<SizeGrade, Long> ensureAllGrades(Map<SizeGrade, Long> distribution) {
        Map<SizeGrade, Long> result = new EnumMap<>(SizeGrade.class);
        for (SizeGrade grade : SizeGrade.values()) {
            result.put(grade, distribution.getOrDefault(grade, 0L));
        }
        return result;
    }

    private CorrelationStatistics calculateSizeReviewWaitCorrelation(List<PrSizeCorrelationDataDto> data) {
        List<double[]> validPairs = data.stream()
                .filter(d -> d.sizeScore() != null && d.reviewWaitMinutes() != null)
                .map(d -> new double[]{d.sizeScore().doubleValue(), d.reviewWaitMinutes().doubleValue()})
                .toList();

        if (validPairs.size() < 3) {
            return CorrelationStatistics.empty();
        }

        double[] x = validPairs.stream().mapToDouble(p -> p[0]).toArray();
        double[] y = validPairs.stream().mapToDouble(p -> p[1]).toArray();

        double correlation = calculatePearsonCorrelation(x, y);
        return CorrelationStatistics.of(correlation);
    }

    private CorrelationStatistics calculateSizeReviewRoundTripCorrelation(List<PrSizeCorrelationDataDto> data) {
        List<double[]> validPairs = data.stream()
                .filter(d -> d.sizeScore() != null && d.reviewRoundTrips() != null)
                .map(d -> new double[]{d.sizeScore().doubleValue(), d.reviewRoundTrips().doubleValue()})
                .toList();

        if (validPairs.size() < 3) {
            return CorrelationStatistics.empty();
        }

        double[] x = validPairs.stream().mapToDouble(p -> p[0]).toArray();
        double[] y = validPairs.stream().mapToDouble(p -> p[1]).toArray();

        double correlation = calculatePearsonCorrelation(x, y);
        return CorrelationStatistics.of(correlation);
    }

    private double calculatePearsonCorrelation(double[] x, double[] y) {
        int n = x.length;
        if (n != y.length || n < 2) {
            return Double.NaN;
        }

        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;
        double sumY2 = 0.0;

        for (int i = 0; i < n; i++) {
            sumX += x[i];
            sumY += y[i];
            sumXY += x[i] * y[i];
            sumX2 += x[i] * x[i];
            sumY2 += y[i] * y[i];
        }

        double numerator = n * sumXY - sumX * sumY;
        double denominator = Math.sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY));

        if (denominator == 0) {
            return Double.NaN;
        }

        return numerator / denominator;
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
