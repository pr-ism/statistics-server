package com.prism.statistics.domain.metric;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum PullRequestSizeCategory {

    SMALL(0, 100),
    MEDIUM(101, 300),
    LARGE(301, 700),
    EXTRA_LARGE(701, Integer.MAX_VALUE);

    private final int minLines;
    private final int maxLines;

    PullRequestSizeCategory(int minLines, int maxLines) {
        this.minLines = minLines;
        this.maxLines = maxLines;
    }

    public static PullRequestSizeCategory classify(int totalLines) {
        return Arrays.stream(values())
                .filter(category -> totalLines >= category.minLines && totalLines <= category.maxLines)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("분류할 수 없는 라인 수입니다: " + totalLines));
    }
}
