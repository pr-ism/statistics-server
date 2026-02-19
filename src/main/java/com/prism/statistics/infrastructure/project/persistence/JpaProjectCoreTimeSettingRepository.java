package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.setting.ProjectCoreTimeSetting;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaProjectCoreTimeSettingRepository extends ListCrudRepository<ProjectCoreTimeSetting, Long> {

    Optional<ProjectCoreTimeSetting> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
