package com.prism.statistics.domain.pullrequest.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PrState {
    DRAFT((state, merged, draft) -> draft),
    MERGED((state, merged, draft) -> "closed".equalsIgnoreCase(state) && merged),
    CLOSED((state, merged, draft) -> "closed".equalsIgnoreCase(state)),
    OPEN((state, merged, draft) -> "open".equalsIgnoreCase(state));

    private final PrStateMatcher matcher;

    public static PrState create(String githubState, boolean isMerged, boolean isDraft) {
        return Arrays.stream(values())
                .filter(prState -> prState.matcher.match(githubState, isMerged, isDraft))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 PR 상태입니다: " + githubState));
    }

    @FunctionalInterface
    interface PrStateMatcher {
        boolean match(String state, boolean isMerged, boolean isDraft);
    }

    public boolean isDraft() {
        return this == DRAFT;
    }

    public boolean isOpen() {
        return this == OPEN;
    }

    public boolean isMerged() {
        return this == MERGED;
    }

    public boolean isClosed() {
        return this == CLOSED;
    }
}
