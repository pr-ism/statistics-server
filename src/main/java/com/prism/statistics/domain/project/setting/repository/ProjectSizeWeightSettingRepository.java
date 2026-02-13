package com.prism.statistics.domain.project.setting.repository;

import com.prism.statistics.domain.project.setting.ProjectSizeWeightSetting;
import java.util.Optional;

public interface ProjectSizeWeightSettingRepository {

    ProjectSizeWeightSetting save(ProjectSizeWeightSetting setting);

    Optional<ProjectSizeWeightSetting> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
