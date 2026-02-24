package com.prism.statistics.domain.analysis.metadata.pullrequest;

import com.prism.statistics.domain.common.CreatedAtEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "commits")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Commit extends CreatedAtEntity {

    private Long githubPullRequestId;

    private Long pullRequestId;

    private String commitSha;

    private LocalDateTime committedAt;

    public static Commit create(Long pullRequestId, Long githubPullRequestId, String commitSha, LocalDateTime committedAt) {
        validatePullRequestId(pullRequestId);
        validateGithubPullRequestId(githubPullRequestId);
        validateCommitSha(commitSha);
        validateCommittedAt(committedAt);
        return new Commit(pullRequestId, githubPullRequestId, commitSha, committedAt);
    }

    public static Commit createEarly(Long githubPullRequestId, String commitSha, LocalDateTime committedAt) {
        validateGithubPullRequestId(githubPullRequestId);
        validateCommitSha(commitSha);
        validateCommittedAt(committedAt);
        return new Commit(null, githubPullRequestId, commitSha, committedAt);
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

    private static void validateCommitSha(String commitSha) {
        if (commitSha == null || commitSha.isBlank()) {
            throw new IllegalArgumentException("커밋 SHA는 필수입니다.");
        }
    }

    private static void validateCommittedAt(LocalDateTime committedAt) {
        if (committedAt == null) {
            throw new IllegalArgumentException("커밋 시각은 필수입니다.");
        }
    }

    private Commit(Long pullRequestId, Long githubPullRequestId, String commitSha, LocalDateTime committedAt) {
        this.pullRequestId = pullRequestId;
        this.githubPullRequestId = githubPullRequestId;
        this.commitSha = commitSha;
        this.committedAt = committedAt;
    }
}
