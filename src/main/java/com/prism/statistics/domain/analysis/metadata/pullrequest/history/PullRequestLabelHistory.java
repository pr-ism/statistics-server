package com.prism.statistics.domain.analysis.metadata.pullrequest.history;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestLabelAction;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pull_request_label_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestLabelHistory extends CreatedAtEntity {

    private Long githubPullRequestId;

    private Long pullRequestId;

    private String headCommitSha;

    private String labelName;

    @Enumerated(EnumType.STRING)
    private PullRequestLabelAction action;

    private LocalDateTime githubChangedAt;

    public static PullRequestLabelHistory create(
            Long githubPullRequestId,
            String headCommitSha,
            String labelName,
            PullRequestLabelAction action,
            LocalDateTime githubChangedAt
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateLabelName(labelName);
        validateAction(action);
        validateChangedAt(githubChangedAt);
        return new PullRequestLabelHistory(githubPullRequestId, headCommitSha, labelName, action, githubChangedAt);
    }

    public void assignPullRequestId(Long pullRequestId) {
        if (this.pullRequestId == null) {
            this.pullRequestId = pullRequestId;
        }
    }

    private static void validateGithubPullRequestId(Long githubPullRequestId) {
        if (githubPullRequestId == null) {
            throw new IllegalArgumentException("GitHub PullRequest ID는 필수입니다.");
        }
    }

    private static void validateHeadCommitSha(String headCommitSha) {
        if (headCommitSha == null || headCommitSha.isBlank()) {
            throw new IllegalArgumentException("Head Commit SHA는 필수입니다.");
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

    private static void validateChangedAt(LocalDateTime githubChangedAt) {
        if (githubChangedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PullRequestLabelHistory(
            Long githubPullRequestId,
            String headCommitSha,
            String labelName,
            PullRequestLabelAction action,
            LocalDateTime githubChangedAt
    ) {
        this.githubPullRequestId = githubPullRequestId;
        this.headCommitSha = headCommitSha;
        this.labelName = labelName;
        this.action = action;
        this.githubChangedAt = githubChangedAt;
    }
}
