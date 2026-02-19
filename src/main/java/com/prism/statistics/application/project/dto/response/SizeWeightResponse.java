package com.prism.statistics.application.project.dto.response;

import com.prism.statistics.domain.project.setting.ProjectSizeWeightSetting;
import java.math.BigDecimal;

public record SizeWeightResponse(BigDecimal additionWeight, BigDecimal deletionWeight, BigDecimal fileWeight) {

    public static SizeWeightResponse from(ProjectSizeWeightSetting setting) {
        return new SizeWeightResponse(
                setting.getWeight().getAdditionWeight(),
                setting.getWeight().getDeletionWeight(),
                setting.getWeight().getFileWeight()
        );
    }
}
