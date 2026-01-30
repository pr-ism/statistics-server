package com.prism.statistics.domain.label;

import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pull_request_labels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestLabel extends CreatedAtEntity {
    private Long pullRequestId;

    private String labelName;

    private LocalDateTime labeledAt;

    public static PullRequestLabel create(
            Long pullRequestId,
            String labelName,
            LocalDateTime labeledAt
    ) {
        validatePullRequestId(pullRequestId);
        validateLabelName(labelName);
        validateLabeledAt(labeledAt);
        return new PullRequestLabel(pullRequestId, labelName, labeledAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PR ID는 필수입니다.");
        }
    }

    private static void validateLabelName(String labelName) {
        if (labelName == null || labelName.isBlank()) {
            throw new IllegalArgumentException("라벨 이름은 필수입니다.");
        }
    }

    private static void validateLabeledAt(LocalDateTime labeledAt) {
        if (labeledAt == null) {
            throw new IllegalArgumentException("라벨 추가 시각은 필수입니다.");
        }
    }

    private PullRequestLabel(
            Long pullRequestId,
            String labelName,
            LocalDateTime labeledAt
    ) {
        this.pullRequestId = pullRequestId;
        this.labelName = labelName;
        this.labeledAt = labeledAt;
    }
}
