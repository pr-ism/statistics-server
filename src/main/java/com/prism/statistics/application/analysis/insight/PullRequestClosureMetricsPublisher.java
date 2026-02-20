package com.prism.statistics.application.analysis.insight;

import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import java.time.LocalDateTime;

public interface PullRequestClosureMetricsPublisher {

    void publish(Long pullRequestId, PullRequestState newState, LocalDateTime closedAt);
}
