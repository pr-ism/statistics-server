package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryAdapter implements ProjectRepository {

    private final JpaProjectRepository jpaProjectRepository;

    @Override
    public Project save(Project project) {
        return jpaProjectRepository.save(project);
    }

    @Override
    public List<Project> findByUserId(Long userId) {
        return jpaProjectRepository.findByUserId(userId);
    }
}
