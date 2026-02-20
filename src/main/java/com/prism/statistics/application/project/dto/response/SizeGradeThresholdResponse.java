package com.prism.statistics.application.project.dto.response;

import com.prism.statistics.domain.project.setting.ProjectSizeGradeThresholdSetting;

public record SizeGradeThresholdResponse(int sThreshold, int mThreshold, int lThreshold, int xlThreshold) {

    public static SizeGradeThresholdResponse from(ProjectSizeGradeThresholdSetting setting) {
        return new SizeGradeThresholdResponse(
                setting.getThreshold().getSThreshold(),
                setting.getThreshold().getMThreshold(),
                setting.getThreshold().getLThreshold(),
                setting.getThreshold().getXlThreshold()
        );
    }
}
