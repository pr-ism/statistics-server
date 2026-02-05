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

    private LocalDateTime pullRequestCreatedAt;

    private LocalDateTime mergedAt;

    private LocalDateTime closedAt;

    public static PullRequestTiming createOpen(LocalDateTime pullRequestCreatedAt) {
        validateCreatedAt(pullRequestCreatedAt);
        return new PullRequestTiming(pullRequestCreatedAt, null, null);
    }

    public static PullRequestTiming createDraft(LocalDateTime pullRequestCreatedAt) {
        validateCreatedAt(pullRequestCreatedAt);
        return new PullRequestTiming(pullRequestCreatedAt, null, null);
    }

    public static PullRequestTiming createReopened(LocalDateTime pullRequestCreatedAt) {
        validateCreatedAt(pullRequestCreatedAt);
        return new PullRequestTiming(pullRequestCreatedAt, null, null);
    }

    public static PullRequestTiming createMerged(LocalDateTime pullRequestCreatedAt, LocalDateTime mergedAt) {
        validateCreatedAt(pullRequestCreatedAt);
        validateMergedAt(pullRequestCreatedAt, mergedAt);
        return new PullRequestTiming(pullRequestCreatedAt, mergedAt, mergedAt);
    }

    public static PullRequestTiming createClosed(LocalDateTime pullRequestCreatedAt, LocalDateTime closedAt) {
        validateCreatedAt(pullRequestCreatedAt);
        validateClosedAt(pullRequestCreatedAt, closedAt);
        return new PullRequestTiming(pullRequestCreatedAt, null, closedAt);
    }

    private static void validateCreatedAt(LocalDateTime pullRequestCreatedAt) {
        if (pullRequestCreatedAt == null) {
            throw new IllegalArgumentException("PullRequest 생성 시각은 필수입니다.");
        }
    }

    private static void validateMergedAt(LocalDateTime pullRequestCreatedAt, LocalDateTime mergedAt) {
        if (mergedAt == null) {
            throw new IllegalArgumentException("병합 시각은 필수입니다.");
        }
        if (pullRequestCreatedAt.isAfter(mergedAt)) {
            throw new IllegalArgumentException("병합 시각은 생성 시각 이후여야 합니다.");
        }
    }

    private static void validateClosedAt(LocalDateTime pullRequestCreatedAt, LocalDateTime closedAt) {
        if (closedAt == null) {
            throw new IllegalArgumentException("종료 시각은 필수입니다.");
        }
        if (pullRequestCreatedAt.isAfter(closedAt)) {
            throw new IllegalArgumentException("종료 시각은 생성 시각 이후여야 합니다.");
        }
    }

    private PullRequestTiming(LocalDateTime pullRequestCreatedAt, LocalDateTime mergedAt, LocalDateTime closedAt) {
        this.pullRequestCreatedAt = pullRequestCreatedAt;
        this.mergedAt = mergedAt;
        this.closedAt = closedAt;
    }

    public long calculateMergeTimeMinutes() {
        if (mergedAt == null) {
            throw new IllegalStateException("병합되지 않은 PullRequest입니다.");
        }
        return Duration.between(pullRequestCreatedAt, mergedAt).toMinutes();
    }
}
