package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final JpaProjectRepository jpaProjectRepository;

    @Override
    public Project save(Project project) {
        return jpaProjectRepository.save(project);
    }
}
