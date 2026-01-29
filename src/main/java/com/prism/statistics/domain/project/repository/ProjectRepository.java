package com.prism.statistics.domain.project.repository;

import com.prism.statistics.domain.project.Project;
import java.util.Optional;

import java.util.List;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Long> findIdByApiKey(String apiKey);

    List<Project> findByUserId(Long userId);

    Optional<Long> existsByIdAndUserId(Long projectId, Long userId);
}
