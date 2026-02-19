package com.prism.statistics.domain.project.setting.vo;

import jakarta.persistence.Embeddable;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CoreTime {

    public static final CoreTime DEFAULT = new CoreTime(
            LocalTime.of(10, 0),
            LocalTime.of(18, 0)
    );

    private LocalTime startTime;

    private LocalTime endTime;

    public static CoreTime of(LocalTime startTime, LocalTime endTime) {
        validateTime(startTime, "시작 시간");
        validateTime(endTime, "종료 시간");
        validateTimeOrder(startTime, endTime);

        return new CoreTime(startTime, endTime);
    }

    public static CoreTime defaultCoreTime() {
        return DEFAULT;
    }

    private static void validateTime(LocalTime time, String fieldName) {
        if (time == null) {
            throw new IllegalArgumentException(fieldName + "은 필수입니다.");
        }
    }

    private static void validateTimeOrder(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
        }
    }

    private CoreTime(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean contains(LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("시간은 필수입니다.");
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }
}
