package com.prism.statistics.domain.pullrequest.vo;

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
public class PrTiming {

    private LocalDateTime prCreatedAt;

    private LocalDateTime mergedAt;

    private LocalDateTime closedAt;

    public static PrTiming createOpen(LocalDateTime prCreatedAt) {
        validateCreatedAt(prCreatedAt);
        return new PrTiming(prCreatedAt, null, null);
    }

    public static PrTiming createDraft(LocalDateTime prCreatedAt) {
        validateCreatedAt(prCreatedAt);
        return new PrTiming(prCreatedAt, null, null);
    }

    public static PrTiming createMerged(LocalDateTime prCreatedAt, LocalDateTime mergedAt, LocalDateTime closedAt) {
        validateCreatedAt(prCreatedAt);
        validateMergedAt(prCreatedAt, mergedAt);
        validateClosedAt(prCreatedAt, closedAt);
        return new PrTiming(prCreatedAt, mergedAt, closedAt);
    }

    public static PrTiming createClosed(LocalDateTime prCreatedAt, LocalDateTime closedAt) {
        validateCreatedAt(prCreatedAt);
        validateClosedAt(prCreatedAt, closedAt);
        return new PrTiming(prCreatedAt, null, closedAt);
    }

    private static void validateCreatedAt(LocalDateTime prCreatedAt) {
        if (prCreatedAt == null) {
            throw new IllegalArgumentException("PR 생성 시각은 필수입니다.");
        }
    }

    private static void validateMergedAt(LocalDateTime prCreatedAt, LocalDateTime mergedAt) {
        if (mergedAt == null) {
            throw new IllegalArgumentException("병합 시각은 필수입니다.");
        }
        if (prCreatedAt.isAfter(mergedAt)) {
            throw new IllegalArgumentException("병합 시각은 생성 시각 이후여야 합니다.");
        }
    }

    private static void validateClosedAt(LocalDateTime prCreatedAt, LocalDateTime closedAt) {
        if (closedAt == null) {
            throw new IllegalArgumentException("종료 시각은 필수입니다.");
        }
        if (prCreatedAt.isAfter(closedAt)) {
            throw new IllegalArgumentException("종료 시각은 생성 시각 이후여야 합니다.");
        }
    }

    private PrTiming(LocalDateTime prCreatedAt, LocalDateTime mergedAt, LocalDateTime closedAt) {
        this.prCreatedAt = prCreatedAt;
        this.mergedAt = mergedAt;
        this.closedAt = closedAt;
    }

    public long calculateMergeTimeMinutes() {
        if (mergedAt == null) {
            throw new IllegalStateException("병합되지 않은 PR입니다.");
        }
        return Duration.between(prCreatedAt, mergedAt).toMinutes();
    }
}
