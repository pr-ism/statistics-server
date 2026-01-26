package com.prism.statistics.application.project.dto.response;

import com.prism.statistics.domain.project.Project;
import lombok.Getter;

@Getter
public class ProjectResponse {
    private final Long id;
    private final String name;

    public ProjectResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName()
        );
    }
}
