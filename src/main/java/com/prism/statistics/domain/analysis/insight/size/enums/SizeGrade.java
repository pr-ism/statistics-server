package com.prism.statistics.domain.analysis.insight.size.enums;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;

@Getter
public enum SizeGrade {

    XS("Extra Small", 0, 10),
    S("Small", 10, 100),
    M("Medium", 100, 300),
    L("Large", 300, 1000),
    XL("Extra Large", 1000, Integer.MAX_VALUE);

    private final String description;
    private final int minScore;
    private final int maxScore;

    SizeGrade(String description, int minScore, int maxScore) {
        this.description = description;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static SizeGrade fromScore(BigDecimal score) {
        if (score == null) {
            throw new IllegalArgumentException("점수는 필수입니다.");
        }
        return fromScore(score.setScale(0, RoundingMode.HALF_UP).intValue());
    }

    public static SizeGrade fromScore(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("점수는 0보다 작을 수 없습니다.");
        }

        for (SizeGrade grade : values()) {
            if (score >= grade.minScore && score < grade.maxScore) {
                return grade;
            }
        }

        return XL;
    }

    public static SizeGrade fromScoreWithThresholds(int score, int[] thresholds) {
        if (thresholds == null || thresholds.length != 4) {
            throw new IllegalArgumentException("임계값 배열은 4개 요소가 필요합니다.");
        }

        if (score < 0) {
            throw new IllegalArgumentException("점수는 0보다 작을 수 없습니다.");
        }

        for (int i = 1; i < thresholds.length; i++) {
            if (thresholds[i] <= thresholds[i - 1]) {
                throw new IllegalArgumentException("임계값은 오름차순이어야 합니다.");
            }
        }

        if (score < thresholds[0]) {
            return XS;
        }
        if (score < thresholds[1]) {
            return S;
        }
        if (score < thresholds[2]) {
            return M;
        }
        if (score < thresholds[3]) {
            return L;
        }
        return XL;
    }

    public boolean isLargeOrAbove() {
        return this == L || this == XL;
    }
}
