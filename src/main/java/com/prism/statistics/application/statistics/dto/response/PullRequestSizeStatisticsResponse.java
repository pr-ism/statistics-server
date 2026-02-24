package com.prism.statistics.application.statistics.dto.response;

import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;

import java.util.EnumMap;
import java.util.Map;

public record PullRequestSizeStatisticsResponse(
        long totalPullRequestCount,
        double avgSizeScore,
        Map<SizeGrade, Long> sizeGradeDistribution,
        double largePullRequestRate,
        CorrelationStatistics sizeReviewWaitCorrelation,
        CorrelationStatistics sizeReviewRoundTripCorrelation
) {

    private static final long ZERO_COUNT = 0L;
    private static final double ZERO_RATE = 0.0d;
    private static final double ROUNDING_SCALE = 100.0d;
    private static final String INTERPRETATION_NO_DATA = "데이터 부족";
    private static final String INTERPRETATION_NONE = "상관관계 없음";
    private static final String INTERPRETATION_WEAK_POSITIVE = "약한 양의 상관관계";
    private static final String INTERPRETATION_WEAK_NEGATIVE = "약한 음의 상관관계";
    private static final String INTERPRETATION_MODERATE_POSITIVE = "보통 양의 상관관계";
    private static final String INTERPRETATION_MODERATE_NEGATIVE = "보통 음의 상관관계";
    private static final String INTERPRETATION_STRONG_POSITIVE = "다소 강한 양의 상관관계";
    private static final String INTERPRETATION_STRONG_NEGATIVE = "다소 강한 음의 상관관계";
    private static final String INTERPRETATION_VERY_STRONG_POSITIVE = "강한 양의 상관관계";
    private static final String INTERPRETATION_VERY_STRONG_NEGATIVE = "강한 음의 상관관계";
    private static final double THRESHOLD_NONE = 0.1d;
    private static final double THRESHOLD_WEAK = 0.3d;
    private static final double THRESHOLD_MODERATE = 0.5d;
    private static final double THRESHOLD_STRONG = 0.7d;

    public static PullRequestSizeStatisticsResponse empty() {
        Map<SizeGrade, Long> emptyDistribution = createEmptyDistribution();

        return new PullRequestSizeStatisticsResponse(
                ZERO_COUNT,
                ZERO_RATE,
                emptyDistribution,
                ZERO_RATE,
                CorrelationStatistics.empty(),
                CorrelationStatistics.empty()
        );
    }

    public record CorrelationStatistics(
            double correlationCoefficient,
            String interpretation
    ) {
        public static CorrelationStatistics empty() {
            return new CorrelationStatistics(ZERO_RATE, INTERPRETATION_NO_DATA);
        }

        public static CorrelationStatistics of(double correlationCoefficient) {
            if (Double.isNaN(correlationCoefficient)) {
                return empty();
            }

            double rounded = roundToTwoDecimals(correlationCoefficient);
            String interpretation = interpretCorrelation(rounded);
            return new CorrelationStatistics(rounded, interpretation);
        }

        private static String interpretCorrelation(double coefficient) {
            double absValue = Math.abs(coefficient);

            if (Double.isNaN(coefficient)) {
                return INTERPRETATION_NO_DATA;
            }

            if (absValue < THRESHOLD_NONE) {
                return INTERPRETATION_NONE;
            }

            if (absValue < THRESHOLD_WEAK) {
                return selectCorrelationInterpretation(
                        coefficient,
                        INTERPRETATION_WEAK_POSITIVE,
                        INTERPRETATION_WEAK_NEGATIVE
                );
            }

            if (absValue < THRESHOLD_MODERATE) {
                return selectCorrelationInterpretation(
                        coefficient,
                        INTERPRETATION_MODERATE_POSITIVE,
                        INTERPRETATION_MODERATE_NEGATIVE
                );
            }

            if (absValue < THRESHOLD_STRONG) {
                return selectCorrelationInterpretation(
                        coefficient,
                        INTERPRETATION_STRONG_POSITIVE,
                        INTERPRETATION_STRONG_NEGATIVE
                );
            }

            return selectCorrelationInterpretation(
                    coefficient,
                    INTERPRETATION_VERY_STRONG_POSITIVE,
                    INTERPRETATION_VERY_STRONG_NEGATIVE
            );
        }

        private static double roundToTwoDecimals(double value) {
            if (Double.isNaN(value)) {
                return ZERO_RATE;
            }
            return Math.round(value * ROUNDING_SCALE) / ROUNDING_SCALE;
        }
    }

    private static String selectCorrelationInterpretation(
            double coefficient,
            String positiveInterpretation,
            String negativeInterpretation
    ) {
        if (coefficient > 0) {
            return positiveInterpretation;
        }
        return negativeInterpretation;
    }

    private static Map<SizeGrade, Long> createEmptyDistribution() {
        Map<SizeGrade, Long> emptyDistribution = new EnumMap<>(SizeGrade.class);
        for (SizeGrade grade : SizeGrade.values()) {
            emptyDistribution.put(grade, ZERO_COUNT);
        }
        return emptyDistribution;
    }
}
