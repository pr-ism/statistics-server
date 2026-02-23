package com.prism.statistics.application.statistics.dto.request;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReviewSpeedStatisticsRequest(

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {

    private static final String INVALID_DATE_RANGE_MESSAGE = "종료일은 시작일보다 빠를 수 없습니다.";

    @AssertTrue(message = INVALID_DATE_RANGE_MESSAGE)
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return !startDate.isAfter(endDate);
    }
}
