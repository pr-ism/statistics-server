package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import com.prism.statistics.application.analysis.metadata.pullrequest.dto.request.PullRequestOpenedRequest.FileData;

import java.time.Instant;
import java.util.List;

public record PullRequestSynchronizedRequest(
        int pullRequestNumber,
        String headCommitSha,
        int additions,
        int deletions,
        int changedFiles,
        CommitsData commits,
        List<FileData> files
) {

    public record CommitsData(int totalCount, List<CommitNode> nodes) {}

    public record CommitNode(String sha, Instant committedDate) {}
}
