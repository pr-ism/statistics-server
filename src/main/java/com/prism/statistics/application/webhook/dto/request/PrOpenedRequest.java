package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;
import java.util.List;

public record PrOpenedRequest(
        int prNumber,
        String title,
        String authorGithubId,
        String link,
        int additions,
        int deletions,
        int changedFileCount,
        int commitCount,
        Instant createdAt,
        List<FileChange> files,
        List<CommitInfo> commits,
        String repositoryFullName
) {

    public record FileChange(
            String fileName,
            String fileStatus,
            int fileAdditions,
            int fileDeletions
    ) {}

    public record CommitInfo(
            String commitSha,
            Instant committedAt
    ) {}
}
