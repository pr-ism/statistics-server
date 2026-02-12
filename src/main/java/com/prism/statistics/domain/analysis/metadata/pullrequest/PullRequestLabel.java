package com.prism.statistics.domain.analysis.metadata.pullrequest;

import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pull_request_labels")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestLabel extends CreatedAtEntity {

    private Long githubPullRequestId;

    private Long pullRequestId;

    private String headCommitSha;

    private String labelName;

    private LocalDateTime labeledAt;

    public static PullRequestLabel create(
            Long githubPullRequestId,
            String headCommitSha,
            String labelName,
            LocalDateTime labeledAt
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateLabelName(labelName);
        validateLabeledAt(labeledAt);
        return new PullRequestLabel(githubPullRequestId, headCommitSha, labelName, labeledAt);
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

    private static void validateLabeledAt(LocalDateTime labeledAt) {
        if (labeledAt == null) {
            throw new IllegalArgumentException("라벨 추가 시각은 필수입니다.");
        }
    }

    private PullRequestLabel(
            Long githubPullRequestId,
            String headCommitSha,
            String labelName,
            LocalDateTime labeledAt
    ) {
        this.githubPullRequestId = githubPullRequestId;
        this.headCommitSha = headCommitSha;
        this.labelName = labelName;
        this.labeledAt = labeledAt;
    }
}
