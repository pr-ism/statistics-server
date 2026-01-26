package com.prism.statistics.application.project.dto.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ProjectListResponse {
    private final List<ProjectResponse> projects;

    public ProjectListResponse(List<ProjectResponse> projects) {
        this.projects = projects;
    }
}
