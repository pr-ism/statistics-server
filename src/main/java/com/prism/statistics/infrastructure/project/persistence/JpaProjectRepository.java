package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.Project;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface JpaProjectRepository extends ListCrudRepository<Project, Long> {
    List<Project> findByUserId(Long userId);
}
