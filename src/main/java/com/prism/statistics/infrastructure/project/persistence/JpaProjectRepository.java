package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.Project;
import org.springframework.data.repository.CrudRepository;

public interface JpaProjectRepository extends CrudRepository<Project, Long> {
}
