package com.prism.statistics.application.metric.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record HotFileStatisticsRequest(

        @Positive(message = "limit는 1 이상이어야 합니다.")
        Integer limit,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {

    private static final int DEFAULT_LIMIT = 10;

    public int limitOrDefault() {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return limit;
    }

    @AssertTrue(message = "종료일은 시작일보다 빠를 수 없습니다.")
    public boolean isDateRangeValid() {
        if (startDate == null || endDate == null) {
            return true;
        }

        return !startDate.isAfter(endDate);
    }
}
