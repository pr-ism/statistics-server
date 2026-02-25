package com.prism.statistics.application.statistics.dto.request;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record WeeklyTrendStatisticsRequest(

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {

    @AssertTrue(message = "시작일과 종료일은 둘 다 입력하거나 둘 다 생략해야 하며, 종료일은 시작일보다 빠를 수 없습니다.")
    public boolean isDateRangeValid() {
        if (startDate == null && endDate == null) {
            return true;
        }

        if (startDate == null || endDate == null) {
            return false;
        }

        return !startDate.isAfter(endDate);
    }
}
