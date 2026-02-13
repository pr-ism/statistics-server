package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.setting.ProjectSizeWeightSetting;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaProjectSizeWeightSettingRepository extends ListCrudRepository<ProjectSizeWeightSetting, Long> {

    Optional<ProjectSizeWeightSetting> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
