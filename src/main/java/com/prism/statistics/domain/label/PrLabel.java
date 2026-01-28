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
@Table(name = "pr_labels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrLabel extends CreatedAtEntity {
    private Long pullRequestId;

    private String labelName;

    private LocalDateTime labeledAt;

    public static PrLabel create(
            Long pullRequestId,
            String labelName,
            LocalDateTime labeledAt
    ) {
        validatePullRequestId(pullRequestId);
        validateLabelName(labelName);
        validateLabeledAt(labeledAt);
        return new PrLabel(pullRequestId, labelName, labeledAt);
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

    private PrLabel(
            Long pullRequestId,
            String labelName,
            LocalDateTime labeledAt
    ) {
        this.pullRequestId = pullRequestId;
        this.labelName = labelName;
        this.labeledAt = labeledAt;
    }
}
