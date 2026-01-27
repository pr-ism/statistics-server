package com.prism.statistics.domain.project.repository;

import com.prism.statistics.domain.project.Project;

import java.util.List;

public interface ProjectRepository {

    Project save(Project project);

    List<Project> findByUserId(Long userId);
}
