package com.prism.statistics.application.project.dto.response;

import com.prism.statistics.domain.project.Project;
import java.util.List;

public record ProjectListResponse(
        List<ProjectResponse> projects
) {
    public record ProjectResponse(
            Long id,
            String name
    ) {
        public static ProjectResponse from(Project project) {
            return new ProjectResponse(
                    project.getId(),
                    project.getName()
            );
        }
    }
}
