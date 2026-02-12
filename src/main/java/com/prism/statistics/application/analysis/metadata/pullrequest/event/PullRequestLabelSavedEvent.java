package com.prism.statistics.application.analysis.metadata.pullrequest.event;

import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestLabel;

public record PullRequestLabelSavedEvent(PullRequestLabel pullRequestLabel) {
}
