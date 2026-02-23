package com.prism.statistics.application.analysis.metadata.pullrequest.event;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;

import java.time.LocalDateTime;
import java.util.List;

public record PullRequestOpenCreatedEvent(
        Long pullRequestId,
        Long githubPullRequestId,
        Long projectId,
        String headCommitSha,
        PullRequestState initialState,
        PullRequestChangeStats changeStats,
        int commitCount,
        LocalDateTime githubCreatedAt,
        List<FileData> files,
        List<CommitData> commits
) {

    public record CommitData(String sha, LocalDateTime committedAt) {
    }
}
