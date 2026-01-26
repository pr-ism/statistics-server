package com.prism.statistics.domain.pullrequest.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum FileChangeType {
    MODIFIED("modified"),
    ADDED("added"),
    REMOVED("removed"),
    RENAMED("renamed");

    private final String githubStatus;

    public static FileChangeType fromGitHubStatus(String status) {
        return Arrays.stream(values())
                .filter(type -> type.githubStatus.equalsIgnoreCase(status))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 파일 변경 타입입니다: " + status));
    }
}
