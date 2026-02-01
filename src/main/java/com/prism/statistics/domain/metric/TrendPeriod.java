package com.prism.statistics.domain.metric;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public enum TrendPeriod {

    WEEKLY,
    MONTHLY;

    public LocalDate periodStartOf(LocalDate date) {
        return switch (this) {
            case WEEKLY -> date.with(DayOfWeek.MONDAY);
            case MONTHLY -> date.withDayOfMonth(1);
        };
    }

    public LocalDate nextPeriodStart(LocalDate periodStart) {
        return switch (this) {
            case WEEKLY -> periodStart.plusWeeks(1);
            case MONTHLY -> periodStart.plusMonths(1);
        };
    }

    public List<LocalDate> generatePeriodStarts(LocalDate startDate, LocalDate endDate) {
        List<LocalDate> periods = new ArrayList<>();
        LocalDate current = periodStartOf(startDate);
        LocalDate lastPeriodStart = periodStartOf(endDate);

        while (!current.isAfter(lastPeriodStart)) {
            periods.add(current);
            current = nextPeriodStart(current);
        }

        return periods;
    }
}
