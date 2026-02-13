package com.prism.statistics.domain.analysis.metadata.pullrequest.vo;

import jakarta.persistence.Embeddable;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PullRequestTiming {

    private LocalDateTime githubCreatedAt;

    private LocalDateTime githubMergedAt;

    private LocalDateTime githubClosedAt;

    public static PullRequestTiming createOpen(LocalDateTime githubCreatedAt) {
        validateCreatedAt(githubCreatedAt);
        return new PullRequestTiming(githubCreatedAt, null, null);
    }

    public static PullRequestTiming createDraft(LocalDateTime githubCreatedAt) {
        validateCreatedAt(githubCreatedAt);
        return new PullRequestTiming(githubCreatedAt, null, null);
    }

    public static PullRequestTiming createReopened(LocalDateTime githubCreatedAt) {
        validateCreatedAt(githubCreatedAt);
        return new PullRequestTiming(githubCreatedAt, null, null);
    }

    public static PullRequestTiming createMerged(LocalDateTime githubCreatedAt, LocalDateTime githubMergedAt) {
        validateCreatedAt(githubCreatedAt);
        validateMergedAt(githubCreatedAt, githubMergedAt);
        return new PullRequestTiming(githubCreatedAt, githubMergedAt, githubMergedAt);
    }

    public static PullRequestTiming createClosed(LocalDateTime githubCreatedAt, LocalDateTime githubClosedAt) {
        validateCreatedAt(githubCreatedAt);
        validateClosedAt(githubCreatedAt, githubClosedAt);
        return new PullRequestTiming(githubCreatedAt, null, githubClosedAt);
    }

    private static void validateCreatedAt(LocalDateTime githubCreatedAt) {
        if (githubCreatedAt == null) {
            throw new IllegalArgumentException("PullRequest 생성 시각은 필수입니다.");
        }
    }

    private static void validateMergedAt(LocalDateTime githubCreatedAt, LocalDateTime githubMergedAt) {
        if (githubMergedAt == null) {
            throw new IllegalArgumentException("병합 시각은 필수입니다.");
        }
        if (githubCreatedAt.isAfter(githubMergedAt)) {
            throw new IllegalArgumentException("병합 시각은 생성 시각 이후여야 합니다.");
        }
    }

    private static void validateClosedAt(LocalDateTime githubCreatedAt, LocalDateTime githubClosedAt) {
        if (githubClosedAt == null) {
            throw new IllegalArgumentException("종료 시각은 필수입니다.");
        }
        if (githubCreatedAt.isAfter(githubClosedAt)) {
            throw new IllegalArgumentException("종료 시각은 생성 시각 이후여야 합니다.");
        }
    }

    private PullRequestTiming(LocalDateTime githubCreatedAt, LocalDateTime githubMergedAt, LocalDateTime githubClosedAt) {
        this.githubCreatedAt = githubCreatedAt;
        this.githubMergedAt = githubMergedAt;
        this.githubClosedAt = githubClosedAt;
    }

    public long calculateMergeTimeMinutes() {
        if (githubMergedAt == null) {
            throw new IllegalStateException("병합되지 않은 PullRequest입니다.");
        }
        return Duration.between(githubCreatedAt, githubMergedAt).toMinutes();
    }
}
