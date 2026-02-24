package com.prism.statistics.domain.analysis.metadata.pullrequest;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.FileChanges;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "pull_request_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestFile extends CreatedAtEntity {

    private Long githubPullRequestId;

    private Long pullRequestId;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private FileChangeType changeType;

    @Embedded
    private FileChanges fileChanges;

    public static PullRequestFile create(
            Long pullRequestId,
            Long githubPullRequestId,
            String fileName,
            FileChangeType changeType,
            FileChanges fileChanges
    ) {
        validatePullRequestId(pullRequestId);
        validateGithubPullRequestId(githubPullRequestId);
        validateFileName(fileName);
        validateChangeType(changeType);
        validateFileChanges(fileChanges);
        return new PullRequestFile(pullRequestId, githubPullRequestId, fileName, changeType, fileChanges);
    }

    public static PullRequestFile createEarly(
            Long githubPullRequestId,
            String fileName,
            FileChangeType changeType,
            FileChanges fileChanges
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateFileName(fileName);
        validateChangeType(changeType);
        validateFileChanges(fileChanges);
        return new PullRequestFile(null, githubPullRequestId, fileName, changeType, fileChanges);
    }

    public void assignPullRequestId(Long pullRequestId) {
        if (this.pullRequestId == null) {
            this.pullRequestId = pullRequestId;
        }
    }

    public boolean hasAssignedPullRequestId() {
        return pullRequestId != null;
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PullRequest ID는 필수입니다.");
        }
    }

    private static void validateGithubPullRequestId(Long githubPullRequestId) {
        if (githubPullRequestId == null) {
            throw new IllegalArgumentException("GitHub PullRequest ID는 필수입니다.");
        }
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("파일명은 필수입니다.");
        }
    }

    private static void validateChangeType(FileChangeType changeType) {
        if (changeType == null) {
            throw new IllegalArgumentException("변경 타입은 필수입니다.");
        }
    }

    private static void validateFileChanges(FileChanges fileChanges) {
        if (fileChanges == null) {
            throw new IllegalArgumentException("파일 변경 정보는 필수입니다.");
        }
    }

    private PullRequestFile(
            Long pullRequestId,
            Long githubPullRequestId,
            String fileName,
            FileChangeType changeType,
            FileChanges fileChanges
    ) {
        this.pullRequestId = pullRequestId;
        this.githubPullRequestId = githubPullRequestId;
        this.fileName = fileName;
        this.changeType = changeType;
        this.fileChanges = fileChanges;
    }

    public int getTotalChanges() {
        return fileChanges.getTotalChanges();
    }
}
