package com.prism.statistics.domain.pullrequest.enums;

import java.util.Arrays;

public enum FileChangeType {
    MODIFIED,
    ADDED,
    REMOVED,
    RENAMED;

    public static FileChangeType fromGitHubStatus(String status) {
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 파일 변경 타입입니다: " + status));
    }
}
