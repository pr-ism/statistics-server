package com.prism.statistics.domain.label;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.label.enums.LabelAction;
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
@Table(name = "pr_label_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PrLabelHistory extends CreatedAtEntity {

    private Long pullRequestId;

    private String labelName;

    @Enumerated(EnumType.STRING)
    private LabelAction action;

    private LocalDateTime changedAt;

    public static PrLabelHistory create(
            Long pullRequestId,
            String labelName,
            LabelAction action,
            LocalDateTime changedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateLabelName(labelName);
        validateAction(action);
        validateChangedAt(changedAt);
        return new PrLabelHistory(pullRequestId, labelName, action, changedAt);
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

    private static void validateAction(LabelAction action) {
        if (action == null) {
            throw new IllegalArgumentException("라벨 액션은 필수입니다.");
        }
    }

    private static void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PrLabelHistory(
            Long pullRequestId,
            String labelName,
            LabelAction action,
            LocalDateTime changedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.labelName = labelName;
        this.action = action;
        this.changedAt = changedAt;
    }
}
