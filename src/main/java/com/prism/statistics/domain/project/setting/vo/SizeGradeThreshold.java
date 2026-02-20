package com.prism.statistics.domain.project.setting.vo;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SizeGradeThreshold {

    public static final SizeGradeThreshold DEFAULT = new SizeGradeThreshold(10, 100, 300, 1000);

    private int sThreshold;

    private int mThreshold;

    private int lThreshold;

    private int xlThreshold;

    public static SizeGradeThreshold of(int sThreshold, int mThreshold, int lThreshold, int xlThreshold) {
        validateThreshold(sThreshold, mThreshold, lThreshold, xlThreshold);

        return new SizeGradeThreshold(sThreshold, mThreshold, lThreshold, xlThreshold);
    }

    public static SizeGradeThreshold defaultThreshold() {
        return DEFAULT;
    }

    private static void validateThreshold(int sThreshold, int mThreshold, int lThreshold, int xlThreshold) {
        validatePositive(sThreshold);
        validatePositive(mThreshold);
        validatePositive(lThreshold);
        validatePositive(xlThreshold);
        validateAscendingOrder(sThreshold, mThreshold, lThreshold, xlThreshold);
    }

    private static void validatePositive(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("임계값은 0보다 커야 합니다.");
        }
    }

    private static void validateAscendingOrder(int s, int m, int l, int xl) {
        if (!(s < m && m < l && l < xl)) {
            throw new IllegalArgumentException("임계값은 S < M < L < XL 순서로 오름차순이어야 합니다.");
        }
    }

    private SizeGradeThreshold(int sThreshold, int mThreshold, int lThreshold, int xlThreshold) {
        this.sThreshold = sThreshold;
        this.mThreshold = mThreshold;
        this.lThreshold = lThreshold;
        this.xlThreshold = xlThreshold;
    }

    public int[] toArray() {
        return new int[]{sThreshold, mThreshold, lThreshold, xlThreshold};
    }
}
