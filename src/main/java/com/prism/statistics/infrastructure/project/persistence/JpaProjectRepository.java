package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.Project;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface JpaProjectRepository extends CrudRepository<Project, Long> {
    List<Project> findByUserId(Long userId);
}
