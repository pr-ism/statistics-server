package com.prism.statistics.domain.review.enums;

import java.util.Arrays;

public enum ReviewState {
    APPROVED,
    CHANGES_REQUESTED,
    COMMENTED;

    public static ReviewState from(String value) {
        return Arrays.stream(values())
                .filter(state -> state.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 리뷰 상태입니다: " + value));
    }
}
