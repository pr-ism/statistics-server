package com.prism.statistics.domain.analysis.insight.vo;

import com.prism.statistics.domain.analysis.insight.enums.PullRequestSizeGrade;
import lombok.Getter;

@Getter
public class SizeGradeThresholds {

    private static final int DEFAULT_XS_THRESHOLD = 10;
    private static final int DEFAULT_S_THRESHOLD = 100;
    private static final int DEFAULT_M_THRESHOLD = 300;
    private static final int DEFAULT_L_THRESHOLD = 1000;

    public static final SizeGradeThresholds DEFAULT = new SizeGradeThresholds(
            DEFAULT_XS_THRESHOLD,
            DEFAULT_S_THRESHOLD,
            DEFAULT_M_THRESHOLD,
            DEFAULT_L_THRESHOLD
    );

    private final int xsThreshold;
    private final int sThreshold;
    private final int mThreshold;
    private final int lThreshold;

    private SizeGradeThresholds(int xsThreshold, int sThreshold, int mThreshold, int lThreshold) {
        this.xsThreshold = xsThreshold;
        this.sThreshold = sThreshold;
        this.mThreshold = mThreshold;
        this.lThreshold = lThreshold;
    }

    public static SizeGradeThresholds create(
            int xsThreshold,
            int sThreshold,
            int mThreshold,
            int lThreshold
    ) {
        validateThresholdOrder(xsThreshold, sThreshold, mThreshold, lThreshold);
        return new SizeGradeThresholds(xsThreshold, sThreshold, mThreshold, lThreshold);
    }

    private static void validateThresholdOrder(int xs, int s, int m, int l) {
        if (xs <= 0) {
            throw new IllegalArgumentException("XS 임계값은 0보다 커야 합니다.");
        }
        if (xs >= s) {
            throw new IllegalArgumentException("XS 임계값은 S 임계값보다 작아야 합니다.");
        }
        if (s >= m) {
            throw new IllegalArgumentException("S 임계값은 M 임계값보다 작아야 합니다.");
        }
        if (m >= l) {
            throw new IllegalArgumentException("M 임계값은 L 임계값보다 작아야 합니다.");
        }
    }

    public PullRequestSizeGrade classify(int totalChanges) {
        return PullRequestSizeGrade.classifyWithThresholds(
                totalChanges,
                xsThreshold,
                sThreshold,
                mThreshold,
                lThreshold
        );
    }
}
