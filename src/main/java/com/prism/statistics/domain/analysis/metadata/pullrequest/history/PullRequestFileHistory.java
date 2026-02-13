package com.prism.statistics.domain.analysis.metadata.pullrequest.history;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.FileChanges;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PreviousFileName;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pull_request_file_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestFileHistory extends CreatedAtEntity {

    private Long pullRequestId;

    private String headCommitSha;

    private String fileName;

    @Embedded
    private PreviousFileName previousFileName;

    @Enumerated(EnumType.STRING)
    private FileChangeType changeType;

    @Embedded
    private FileChanges fileChanges;

    private LocalDateTime githubChangedAt;

    public static PullRequestFileHistory create(
            Long pullRequestId,
            String headCommitSha,
            String fileName,
            FileChangeType changeType,
            FileChanges fileChanges,
            LocalDateTime githubChangedAt
    ) {
        validateBaseFields(pullRequestId, headCommitSha, fileName, fileChanges, githubChangedAt);
        validateChangeType(changeType);
        return new PullRequestFileHistory(pullRequestId, headCommitSha, fileName, PreviousFileName.empty(), changeType, fileChanges, githubChangedAt);
    }

    public static PullRequestFileHistory createRenamed(
            Long pullRequestId,
            String headCommitSha,
            String fileName,
            String previousFileName,
            FileChanges fileChanges,
            LocalDateTime githubChangedAt
    ) {
        validateBaseFields(pullRequestId, headCommitSha, fileName, fileChanges, githubChangedAt);
        return new PullRequestFileHistory(pullRequestId, headCommitSha, fileName, PreviousFileName.of(previousFileName), FileChangeType.RENAMED, fileChanges, githubChangedAt);
    }

    private static void validateBaseFields(Long pullRequestId, String headCommitSha, String fileName, FileChanges fileChanges, LocalDateTime githubChangedAt) {
        validatePullRequestId(pullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateFileName(fileName);
        validateFileChanges(fileChanges);
        validateChangedAt(githubChangedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PullRequest ID는 필수입니다.");
        }
    }

    private static void validateHeadCommitSha(String headCommitSha) {
        if (headCommitSha == null || headCommitSha.isBlank()) {
            throw new IllegalArgumentException("Head Commit SHA는 필수입니다.");
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

    private static void validateChangedAt(LocalDateTime githubChangedAt) {
        if (githubChangedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PullRequestFileHistory(
            Long pullRequestId,
            String headCommitSha,
            String fileName,
            PreviousFileName previousFileName,
            FileChangeType changeType,
            FileChanges fileChanges,
            LocalDateTime githubChangedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.headCommitSha = headCommitSha;
        this.fileName = fileName;
        this.previousFileName = previousFileName;
        this.changeType = changeType;
        this.fileChanges = fileChanges;
        this.githubChangedAt = githubChangedAt;
    }

    public boolean isRenamed() {
        return previousFileName.isPresent();
    }

    public int getTotalChanges() {
        return fileChanges.getTotalChanges();
    }
}
