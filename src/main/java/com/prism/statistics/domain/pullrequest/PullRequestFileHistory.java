package com.prism.statistics.domain.pullrequest;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.pullrequest.vo.FileChanges;
import com.prism.statistics.domain.pullrequest.vo.PreviousFileName;
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

    private String fileName;

    @Embedded
    private PreviousFileName previousFileName;

    @Enumerated(EnumType.STRING)
    private FileChangeType changeType;

    @Embedded
    private FileChanges fileChanges;

    private LocalDateTime changedAt;

    public static PullRequestFileHistory create(
            Long pullRequestId,
            String fileName,
            FileChangeType changeType,
            FileChanges fileChanges,
            LocalDateTime changedAt
    ) {
        validateBaseFields(pullRequestId, fileName, fileChanges, changedAt);
        validateChangeType(changeType);
        return new PullRequestFileHistory(pullRequestId, fileName, PreviousFileName.empty(), changeType, fileChanges, changedAt);
    }

    public static PullRequestFileHistory createRenamed(
            Long pullRequestId,
            String fileName,
            String previousFileName,
            FileChanges fileChanges,
            LocalDateTime changedAt
    ) {
        validateBaseFields(pullRequestId, fileName, fileChanges, changedAt);
        return new PullRequestFileHistory(pullRequestId, fileName, PreviousFileName.of(previousFileName), FileChangeType.RENAMED, fileChanges, changedAt);
    }

    private static void validateBaseFields(Long pullRequestId, String fileName, FileChanges fileChanges, LocalDateTime changedAt) {
        validatePullRequestId(pullRequestId);
        validateFileName(fileName);
        validateFileChanges(fileChanges);
        validateChangedAt(changedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PullRequest ID는 필수입니다.");
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

    private static void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PullRequestFileHistory(
            Long pullRequestId,
            String fileName,
            PreviousFileName previousFileName,
            FileChangeType changeType,
            FileChanges fileChanges,
            LocalDateTime changedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.fileName = fileName;
        this.previousFileName = previousFileName;
        this.changeType = changeType;
        this.fileChanges = fileChanges;
        this.changedAt = changedAt;
    }

    public boolean isRenamed() {
        return previousFileName.isPresent();
    }

    public int getTotalChanges() {
        return fileChanges.getTotalChanges();
    }
}
