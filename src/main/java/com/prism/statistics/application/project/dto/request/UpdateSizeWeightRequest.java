package com.prism.statistics.application.project.dto.request;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateSizeWeightRequest(

        @NotNull(message = "추가 라인 가중치는 필수입니다.")
        BigDecimal additionWeight,

        @NotNull(message = "삭제 라인 가중치는 필수입니다.")
        BigDecimal deletionWeight,

        @NotNull(message = "파일 가중치는 필수입니다.")
        BigDecimal fileWeight
) {
}
