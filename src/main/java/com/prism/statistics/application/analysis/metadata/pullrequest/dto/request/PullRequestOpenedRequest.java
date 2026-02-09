package com.prism.statistics.application.analysis.metadata.pullrequest.dto.request;

import java.time.Instant;
import java.util.List;

public record PullRequestOpenedRequest(
        boolean isDraft,
        PullRequestData pullRequest,
        List<FileData> files
) {

    public record PullRequestData(
            Long githubPullRequestId,
            int number,
            String title,
            String url,
            String headCommitSha,
            int additions,
            int deletions,
            int changedFiles,
            Instant createdAt,
            Author author,
            CommitsConnection commits
    ) {}

    public record Author(String login, Long id) {}

    public record CommitsConnection(int totalCount, List<CommitNode> nodes) {}

    public record CommitNode(CommitData commit) {}

    public record CommitData(String oid, Instant committedDate) {}

    public record FileData(
            String filename,
            String status,
            int additions,
            int deletions
    ) {}
}
