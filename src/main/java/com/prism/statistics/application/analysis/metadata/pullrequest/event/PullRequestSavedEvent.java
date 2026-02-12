package com.prism.statistics.application.analysis.metadata.pullrequest.event;

public record PullRequestSavedEvent(Long githubPullRequestId, Long pullRequestId) {
}
