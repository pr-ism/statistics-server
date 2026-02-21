package com.prism.statistics.domain.analysis.metadata.pullrequest.enums;

import java.util.Arrays;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PullRequestState {
    DRAFT((state, merged, draft) -> "open".equalsIgnoreCase(state) && draft),
    MERGED((state, merged, draft) -> "closed".equalsIgnoreCase(state) && merged),
    CLOSED((state, merged, draft) -> "closed".equalsIgnoreCase(state) && !merged),
    OPEN((state, merged, draft) -> "open".equalsIgnoreCase(state) && !draft);

    private final PullRequestStateMatcher matcher;

    public static PullRequestState create(String githubState, boolean isMerged, boolean isDraft) {
        return Arrays.stream(values())
                .filter(pullRequestState -> pullRequestState.matcher.match(githubState, isMerged, isDraft))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 PullRequest 상태입니다: " + githubState));
    }

    @FunctionalInterface
    interface PullRequestStateMatcher {
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

    public boolean isClosureState() {
        return this == MERGED || this == CLOSED;
    }
}
