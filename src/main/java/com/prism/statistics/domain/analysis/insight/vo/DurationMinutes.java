package com.prism.statistics.domain.analysis.insight.vo;

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
public class DurationMinutes {

    private static final long MINUTES_PER_HOUR = 60;
    private static final long MINUTES_PER_DAY = 1440;

    private long minutes;

    public static DurationMinutes of(long minutes) {
        validateMinutes(minutes);
        return new DurationMinutes(minutes);
    }

    public static DurationMinutes between(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("시작 시각과 종료 시각은 필수입니다.");
        }

        if (end.isBefore(start)) {
            throw new IllegalArgumentException("종료 시각은 시작 시각 이후여야 합니다.");
        }

        long minutes = Duration.between(start, end).toMinutes();

        return new DurationMinutes(minutes);
    }

    public static DurationMinutes zero() {
        return new DurationMinutes(0);
    }

    private static void validateMinutes(long minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("소요 시간은 0보다 작을 수 없습니다.");
        }
    }

    private DurationMinutes(long minutes) {
        this.minutes = minutes;
    }

    public DurationMinutes add(DurationMinutes other) {
        if (other == null) {
            return this;
        }

        return new DurationMinutes(this.minutes + other.minutes);
    }

    public DurationMinutes subtract(DurationMinutes other) {
        if (other == null) {
            return this;
        }

        long result = this.minutes - other.minutes;

        if (result < 0) {
            throw new IllegalArgumentException("결과 소요 시간은 0보다 작을 수 없습니다.");
        }

        return new DurationMinutes(result);
    }

    public long toHours() {
        return minutes / MINUTES_PER_HOUR;
    }

    public long toDays() {
        return minutes / MINUTES_PER_DAY;
    }

    public boolean isZero() {
        return minutes == 0;
    }

    public boolean isGreaterThan(DurationMinutes other) {
        if (other == null) {
            throw new IllegalArgumentException("비교 대상은 null일 수 없습니다.");
        }

        return this.minutes > other.minutes;
    }

    public boolean isLessThan(DurationMinutes other) {
        if (other == null) {
            throw new IllegalArgumentException("비교 대상은 null일 수 없습니다.");
        }
        
        return this.minutes < other.minutes;
    }
}
