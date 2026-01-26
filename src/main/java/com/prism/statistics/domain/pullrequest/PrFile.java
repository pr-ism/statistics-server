package com.prism.statistics.domain.pullrequest;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.pullrequest.enums.FileChangeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "pr_files")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrFile extends CreatedAtEntity {

    private Long pullRequestId;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private FileChangeType changeType;

    private int additions;

    private int deletions;

    public static PrFile create(
            Long pullRequestId,
            String fileName,
            FileChangeType changeType,
            int additions,
            int deletions
    ) {
        validatePullRequestId(pullRequestId);
        validateFileName(fileName);
        validateChangeType(changeType);
        validateAdditions(additions);
        validateDeletions(deletions);
        return new PrFile(pullRequestId, fileName, changeType, additions, deletions);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PR ID는 필수입니다.");
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

    private static void validateAdditions(int additions) {
        if (additions < 0) {
            throw new IllegalArgumentException("추가 라인 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateDeletions(int deletions) {
        if (deletions < 0) {
            throw new IllegalArgumentException("삭제 라인 수는 0보다 작을 수 없습니다.");
        }
    }

    private PrFile(
            Long pullRequestId,
            String fileName,
            FileChangeType changeType,
            int additions,
            int deletions
    ) {
        this.pullRequestId = pullRequestId;
        this.fileName = fileName;
        this.changeType = changeType;
        this.additions = additions;
        this.deletions = deletions;
    }

    public int getTotalChanges() {
        return additions + deletions;
    }
}
