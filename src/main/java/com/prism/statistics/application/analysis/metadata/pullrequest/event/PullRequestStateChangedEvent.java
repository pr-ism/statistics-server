package com.prism.statistics.application.analysis.metadata.pullrequest.event;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;

import java.time.LocalDateTime;

public record PullRequestStateChangedEvent(
        Long pullRequestId,
        String headCommitSha,
        PullRequestState previousState,
        PullRequestState newState,
        LocalDateTime githubChangedAt
) {}
