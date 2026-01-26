package com.prism.statistics.application.webhook.dto.request;

import java.time.Instant;
import java.util.List;

public record PrOpenedRequest(
        Integer prNumber,
        String title,
        String state,
        String authorGithubId,
        String link,
        Integer additions,
        Integer deletions,
        Integer changedFileCount,
        Integer commitCount,
        Instant createdAt,
        List<FileChange> files,
        List<CommitInfo> commits,
        String repositoryFullName
) {

    public record FileChange(
            String fileName,
            String fileStatus,
            Integer fileAdditions,
            Integer fileDeletions
    ) {}

    public record CommitInfo(
            String commitSha,
            Instant committedAt
    ) {}
}
