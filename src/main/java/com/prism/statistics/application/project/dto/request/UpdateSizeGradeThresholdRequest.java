package com.prism.statistics.application.project.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateSizeGradeThresholdRequest(

        @NotNull(message = "S 임계값은 필수입니다.")
        @Positive(message = "S 임계값은 0보다 커야 합니다.")
        Integer sThreshold,

        @NotNull(message = "M 임계값은 필수입니다.")
        @Positive(message = "M 임계값은 0보다 커야 합니다.")
        Integer mThreshold,

        @NotNull(message = "L 임계값은 필수입니다.")
        @Positive(message = "L 임계값은 0보다 커야 합니다.")
        Integer lThreshold,

        @NotNull(message = "XL 임계값은 필수입니다.")
        @Positive(message = "XL 임계값은 0보다 커야 합니다.")
        Integer xlThreshold
) {
}
