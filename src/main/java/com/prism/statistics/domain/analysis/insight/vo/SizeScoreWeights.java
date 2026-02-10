package com.prism.statistics.domain.analysis.insight.vo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.Getter;

@Getter
public class SizeScoreWeights {

    private static final BigDecimal DEFAULT_WEIGHT = BigDecimal.ONE;

    public static final SizeScoreWeights DEFAULT = new SizeScoreWeights(
            DEFAULT_WEIGHT,
            DEFAULT_WEIGHT,
            DEFAULT_WEIGHT
    );

    private final BigDecimal additionWeight;
    private final BigDecimal deletionWeight;
    private final BigDecimal changedFileWeight;

    private SizeScoreWeights(
            BigDecimal additionWeight,
            BigDecimal deletionWeight,
            BigDecimal changedFileWeight
    ) {
        this.additionWeight = additionWeight;
        this.deletionWeight = deletionWeight;
        this.changedFileWeight = changedFileWeight;
    }

    public static SizeScoreWeights create(
            BigDecimal additionWeight,
            BigDecimal deletionWeight,
            BigDecimal changedFileWeight
    ) {
        validateWeight(additionWeight, "addition");
        validateWeight(deletionWeight, "deletion");
        validateWeight(changedFileWeight, "changedFile");

        return new SizeScoreWeights(additionWeight, deletionWeight, changedFileWeight);
    }

    private static void validateWeight(BigDecimal weight, String fieldName) {
        if (weight == null) {
            throw new IllegalArgumentException(fieldName + " 가중치는 필수입니다.");
        }
        if (weight.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " 가중치는 0보다 작을 수 없습니다.");
        }
    }

    public BigDecimal calculateScore(int additions, int deletions, int changedFileCount) {
        BigDecimal additionScore = additionWeight.multiply(BigDecimal.valueOf(additions));
        BigDecimal deletionScore = deletionWeight.multiply(BigDecimal.valueOf(deletions));
        BigDecimal changedFileScore = changedFileWeight.multiply(BigDecimal.valueOf(changedFileCount));

        return additionScore.add(deletionScore).add(changedFileScore)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
