package com.prism.statistics.application.analysis.metadata.pullrequest.event;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent.CommitData;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;

import java.time.LocalDateTime;
import java.util.List;

public record PullRequestEarlySynchronizedEvent(
        Long githubPullRequestId,
        String headCommitSha,
        PullRequestChangeStats changeStats,
        int commitCount,
        LocalDateTime githubChangedAt,
        List<FileData> files,
        List<CommitData> commits
) {
}
