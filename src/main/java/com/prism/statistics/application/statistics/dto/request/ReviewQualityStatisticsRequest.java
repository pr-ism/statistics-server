package com.prism.statistics.application.statistics.dto.request;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record ReviewQualityStatisticsRequest(
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {
}
