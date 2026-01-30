package com.prism.statistics.application.webhook.event;

import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest.FileData;
import com.prism.statistics.domain.pullrequest.enums.PrState;
import com.prism.statistics.domain.pullrequest.vo.PullRequestChangeStats;

import java.time.LocalDateTime;
import java.util.List;

public record PrOpenCreatedEvent(
        Long pullRequestId,
        Long projectId,
        PrState initialState,
        PullRequestChangeStats changeStats,
        int commitCount,
        LocalDateTime prCreatedAt,
        List<FileData> files,
        List<CommitData> commits
) {

    public record CommitData(String sha, LocalDateTime committedAt) {
    }
}
