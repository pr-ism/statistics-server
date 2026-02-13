package com.prism.statistics.domain.analysis.insight.size.vo;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SizeScoreWeight {

    public static final SizeScoreWeight DEFAULT = new SizeScoreWeight(
            BigDecimal.ONE,
            BigDecimal.ONE,
            BigDecimal.ONE
    );

    private BigDecimal additionWeight;

    private BigDecimal deletionWeight;

    private BigDecimal fileWeight;

    public static SizeScoreWeight of(BigDecimal additionWeight, BigDecimal deletionWeight, BigDecimal fileWeight) {
        validateWeight(additionWeight, "추가 라인 가중치");
        validateWeight(deletionWeight, "삭제 라인 가중치");
        validateWeight(fileWeight, "파일 가중치");

        return new SizeScoreWeight(additionWeight, deletionWeight, fileWeight);
    }

    public static SizeScoreWeight defaultWeight() {
        return DEFAULT;
    }

    private static void validateWeight(BigDecimal weight, String fieldName) {
        if (weight == null) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        if (weight.signum() < 0) {
            throw new IllegalArgumentException(fieldName + "는 0보다 작을 수 없습니다.");
        }
    }

    private SizeScoreWeight(BigDecimal additionWeight, BigDecimal deletionWeight, BigDecimal fileWeight) {
        this.additionWeight = additionWeight;
        this.deletionWeight = deletionWeight;
        this.fileWeight = fileWeight;
    }

    public BigDecimal calculateScore(int additions, int deletions, int fileCount) {
        BigDecimal additionScore = additionWeight.multiply(BigDecimal.valueOf(additions));
        BigDecimal deletionScore = deletionWeight.multiply(BigDecimal.valueOf(deletions));
        BigDecimal fileScore = fileWeight.multiply(BigDecimal.valueOf(fileCount));

        return additionScore.add(deletionScore).add(fileScore);
    }
}
