package com.prism.statistics.application.project.dto.response;

import com.prism.statistics.domain.project.setting.ProjectCoreTimeSetting;
import java.time.LocalTime;

public record CoreTimeResponse(LocalTime startTime, LocalTime endTime) {

    public static CoreTimeResponse from(ProjectCoreTimeSetting setting) {
        return new CoreTimeResponse(
                setting.getCoreTime().getStartTime(),
                setting.getCoreTime().getEndTime()
        );
    }
}
