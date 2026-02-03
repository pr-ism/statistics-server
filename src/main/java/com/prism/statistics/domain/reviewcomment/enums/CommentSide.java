package com.prism.statistics.domain.reviewcomment.enums;

import java.util.Arrays;

public enum CommentSide {
    LEFT,
    RIGHT;

    public static CommentSide from(String value) {
        return Arrays.stream(values())
                .filter(side -> side.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 CommentSide입니다: " + value));
    }
}
