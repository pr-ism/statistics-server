package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import com.prism.statistics.application.collect.inbox.CollectInboxRequest;
import java.time.Instant;
import java.util.List;

public record PullRequestSynchronizedRequest(
        long runId,
        Long githubPullRequestId,
        int pullRequestNumber,
        String headCommitSha,
        int additions,
        int deletions,
        int changedFiles,
        CommitsData commits,
        List<FileData> files
) implements CollectInboxRequest {

    public record CommitsData(int totalCount, List<CommitNode> nodes) {}

    public record CommitNode(String sha, Instant committedDate) {}

    public record FileData(
            String filename,
            String status,
            int additions,
            int deletions,
            String previousFilename
    ) {}
}
