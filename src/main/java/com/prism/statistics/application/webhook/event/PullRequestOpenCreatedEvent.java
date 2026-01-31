package com.prism.statistics.application.webhook.event;

import com.prism.statistics.application.webhook.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.domain.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;

import java.time.LocalDateTime;
import java.util.List;

public record PullRequestOpenCreatedEvent(
        Long pullRequestId,
        Long projectId,
        PullRequestState initialState,
        PullRequestChangeStats changeStats,
        int commitCount,
        LocalDateTime pullRequestCreatedAt,
        List<FileData> files,
        List<CommitData> commits
) {

    public record CommitData(String sha, LocalDateTime committedAt) {
    }
}
