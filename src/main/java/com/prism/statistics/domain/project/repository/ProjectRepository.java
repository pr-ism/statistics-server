package com.prism.statistics.domain.project.repository;

import com.prism.statistics.domain.project.Project;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Long> findIdByApiKey(String apiKey);

    List<Project> findAllProjectsByUserId(Long userId);

    Optional<Long> findIdByProjectIdAndUserId(Long projectId, Long userId);
}
