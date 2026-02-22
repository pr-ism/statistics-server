package com.prism.statistics.application.statistics.dto.response;

import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import java.util.EnumMap;
import java.util.Map;

public record PrSizeStatisticsResponse(
        long totalPrCount,
        double avgSizeScore,
        Map<SizeGrade, Long> sizeGradeDistribution,
        double largePrRate,
        CorrelationStatistics sizeReviewWaitCorrelation,
        CorrelationStatistics sizeReviewRoundTripCorrelation
) {

    public static PrSizeStatisticsResponse empty() {
        Map<SizeGrade, Long> emptyDistribution = new EnumMap<>(SizeGrade.class);
        for (SizeGrade grade : SizeGrade.values()) {
            emptyDistribution.put(grade, 0L);
        }

        return new PrSizeStatisticsResponse(
                0L,
                0.0,
                emptyDistribution,
                0.0,
                CorrelationStatistics.empty(),
                CorrelationStatistics.empty()
        );
    }

    public record CorrelationStatistics(
            double correlationCoefficient,
            String interpretation
    ) {
        public static CorrelationStatistics empty() {
            return new CorrelationStatistics(0.0, "데이터 부족");
        }

        public static CorrelationStatistics of(double correlationCoefficient) {
            double rounded = roundToTwoDecimals(correlationCoefficient);
            String interpretation = interpretCorrelation(rounded);
            return new CorrelationStatistics(rounded, interpretation);
        }

        private static String interpretCorrelation(double coefficient) {
            double absValue = Math.abs(coefficient);

            if (Double.isNaN(coefficient)) {
                return "데이터 부족";
            }

            if (absValue < 0.1) {
                return "상관관계 없음";
            } else if (absValue < 0.3) {
                return coefficient > 0 ? "약한 양의 상관관계" : "약한 음의 상관관계";
            } else if (absValue < 0.5) {
                return coefficient > 0 ? "보통 양의 상관관계" : "보통 음의 상관관계";
            } else if (absValue < 0.7) {
                return coefficient > 0 ? "다소 강한 양의 상관관계" : "다소 강한 음의 상관관계";
            } else {
                return coefficient > 0 ? "강한 양의 상관관계" : "강한 음의 상관관계";
            }
        }

        private static double roundToTwoDecimals(double value) {
            if (Double.isNaN(value)) {
                return 0.0;
            }
            return Math.round(value * 100.0) / 100.0;
        }
    }
}
