package com.prism.statistics.domain.analysis.insight.enums;

import lombok.Getter;

@Getter
public enum PullRequestSizeGrade {

    XS("XS", 0, 10),
    S("S", 10, 100),
    M("M", 100, 300),
    L("L", 300, 1000),
    XL("XL", 1000, Integer.MAX_VALUE);

    private final String label;
    private final int minExclusive;
    private final int maxInclusive;

    PullRequestSizeGrade(String label, int minExclusive, int maxInclusive) {
        this.label = label;
        this.minExclusive = minExclusive;
        this.maxInclusive = maxInclusive;
    }

    public static PullRequestSizeGrade classify(int totalChanges) {
        if (totalChanges < 10) {
            return XS;
        }
        if (totalChanges < 100) {
            return S;
        }
        if (totalChanges < 300) {
            return M;
        }
        if (totalChanges < 1000) {
            return L;
        }
        return XL;
    }

    public static PullRequestSizeGrade classifyWithThresholds(
            int totalChanges,
            int xsThreshold,
            int sThreshold,
            int mThreshold,
            int lThreshold
    ) {
        if (totalChanges < xsThreshold) {
            return XS;
        }
        if (totalChanges < sThreshold) {
            return S;
        }
        if (totalChanges < mThreshold) {
            return M;
        }
        if (totalChanges < lThreshold) {
            return L;
        }
        return XL;
    }
}
