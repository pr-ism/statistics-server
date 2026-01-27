package com.prism.statistics.domain.project.repository;

import com.prism.statistics.domain.project.Project;
import java.util.Optional;

public interface ProjectRepository {

    Project save(Project project);

    Optional<Long> findIdByApiKey(String apiKey);
}
