package com.prism.statistics.application.project.dto.response;

import com.prism.statistics.domain.project.Project;
import lombok.Getter;

@Getter
public class ProjectResponse {
    private final Long id;
    private final String name;
    private final String apiKey;

    public ProjectResponse(Long id, String name, String apiKey) {
        this.id = id;
        this.name = name;
        this.apiKey = apiKey;
    }

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getApiKey()
        );
    }
}
