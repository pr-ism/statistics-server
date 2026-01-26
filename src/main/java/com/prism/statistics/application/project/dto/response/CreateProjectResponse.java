package com.prism.statistics.application.project.dto.response;

import com.prism.statistics.domain.project.Project;

public record CreateProjectResponse(String apiKey) {

    public static CreateProjectResponse from(Project project) {
        return new CreateProjectResponse(project.getApiKey());
    }
}
