package com.prism.statistics.domain.analysis.metadata.pullrequest.history;

import com.prism.statistics.domain.common.CreatedAtEntity;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "pull_request_content_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestContentHistory extends CreatedAtEntity {

    private Long githubPullRequestId;

    private Long pullRequestId;

    private String headCommitSha;

    @Embedded
    private PullRequestChangeStats changeStats;

    private int commitCount;

    private LocalDateTime githubChangedAt;

    public static PullRequestContentHistory create(
            Long pullRequestId,
            Long githubPullRequestId,
            String headCommitSha,
            PullRequestChangeStats changeStats,
            int commitCount,
            LocalDateTime githubChangedAt
    ) {
        validatePullRequestId(pullRequestId);
        validateGithubPullRequestId(githubPullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateChangeStats(changeStats);
        validateCommitCount(commitCount);
        validateChangedAt(githubChangedAt);
        return new PullRequestContentHistory(pullRequestId, githubPullRequestId, headCommitSha, changeStats, commitCount, githubChangedAt);
    }

    public static PullRequestContentHistory createEarly(
            Long githubPullRequestId,
            String headCommitSha,
            PullRequestChangeStats changeStats,
            int commitCount,
            LocalDateTime githubChangedAt
    ) {
        validateGithubPullRequestId(githubPullRequestId);
        validateHeadCommitSha(headCommitSha);
        validateChangeStats(changeStats);
        validateCommitCount(commitCount);
        validateChangedAt(githubChangedAt);
        return new PullRequestContentHistory(null, githubPullRequestId, headCommitSha, changeStats, commitCount, githubChangedAt);
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

    private static void validateHeadCommitSha(String headCommitSha) {
        if (headCommitSha == null || headCommitSha.isBlank()) {
            throw new IllegalArgumentException("Head Commit SHA는 필수입니다.");
        }
    }

    private static void validateChangeStats(PullRequestChangeStats changeStats) {
        if (changeStats == null) {
            throw new IllegalArgumentException("변경 내역은 필수입니다.");
        }
    }

    private static void validateCommitCount(int commitCount) {
        if (commitCount < 0) {
            throw new IllegalArgumentException("커밋 수는 0보다 작을 수 없습니다.");
        }
    }

    private static void validateChangedAt(LocalDateTime githubChangedAt) {
        if (githubChangedAt == null) {
            throw new IllegalArgumentException("변경 시각은 필수입니다.");
        }
    }

    private PullRequestContentHistory(
            Long pullRequestId,
            Long githubPullRequestId,
            String headCommitSha,
            PullRequestChangeStats changeStats,
            int commitCount,
            LocalDateTime githubChangedAt
    ) {
        this.pullRequestId = pullRequestId;
        this.githubPullRequestId = githubPullRequestId;
        this.headCommitSha = headCommitSha;
        this.changeStats = changeStats;
        this.commitCount = commitCount;
        this.githubChangedAt = githubChangedAt;
    }
}
