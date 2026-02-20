package com.prism.statistics.domain.project.setting.repository;

import com.prism.statistics.domain.project.setting.ProjectSizeGradeThresholdSetting;
import java.util.Optional;

public interface ProjectSizeGradeThresholdSettingRepository {

    ProjectSizeGradeThresholdSetting save(ProjectSizeGradeThresholdSetting setting);

    Optional<ProjectSizeGradeThresholdSetting> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
