package com.prism.statistics.application.metric.dto.request;

import jakarta.validation.constraints.AssertTrue;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record SizeStatisticsRequest(

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {

    @AssertTrue(message = "종료일은 시작일보다 빠를 수 없습니다.")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return !startDate.isAfter(endDate);
    }
}
