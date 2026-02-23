package com.prism.statistics.application.analysis.metadata.pullrequest.event;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestSynchronizedRequest.FileData;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestOpenCreatedEvent.CommitData;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;

import java.time.LocalDateTime;
import java.util.List;

public record PullRequestSynchronizedEvent(
        Long pullRequestId,
        Long githubPullRequestId,
        String headCommitSha,
        boolean isNewer,
        PullRequestChangeStats changeStats,
        int commitCount,
        LocalDateTime githubChangedAt,
        List<FileData> files,
        List<CommitData> newCommits
) {
}
