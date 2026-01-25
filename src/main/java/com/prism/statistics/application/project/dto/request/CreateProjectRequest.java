package com.prism.statistics.application.project.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateProjectRequest(

        @NotBlank(message = "프로젝트 이름은 비어 있을 수 없습니다.")
        String name
) {
}
