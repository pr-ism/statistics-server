package com.prism.statistics.domain.label;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.label.enums.PullRequestLabelAction;
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
@Table(name = "pull_request_label_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestLabelHistory extends CreatedAtEntity {

    private Long pullRequestId;

    private String labelName;

    @Enumerated(EnumType.STRING)
    private PullRequestLabelAction action;

    private LocalDateTime changedAt;

    public static PullRequestLabelHistory create(
            Long pullRequestId,
            String labelName,
            PullRequestLabelAction action,
            LocalDateTime changedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateLabelName(labelName);
        validateAction(action);
        validateChangedAt(changedAt);
        return new PullRequestLabelHistory(pullRequestId, labelName, action, changedAt);
    }

    private static void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("PullRequest ID는 필수입니다.");
        }
    }

    private static void validateLabelName(String labelName) {
        if (labelName == null || labelName.isBlank()) {
            throw new IllegalArgumentException("라벨 이름은 필수입니다.");
        }
    }

    private static void validateAction(PullRequestLabelAction action) {
        if (action == null) {
            throw new IllegalArgumentException("라벨 액션은 필수입니다.");
        }
    }

    private static void validateChangedAt(LocalDateTime changedAt) {
        if (changedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PullRequestLabelHistory(
            Long pullRequestId,
            String labelName,
            PullRequestLabelAction action,
            LocalDateTime changedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.labelName = labelName;
        this.action = action;
        this.changedAt = changedAt;
    }
}
