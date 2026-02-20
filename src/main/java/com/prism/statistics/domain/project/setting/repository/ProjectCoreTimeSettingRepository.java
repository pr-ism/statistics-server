package com.prism.statistics.domain.project.setting.repository;

import com.prism.statistics.domain.project.setting.ProjectCoreTimeSetting;
import java.util.Optional;

public interface ProjectCoreTimeSettingRepository {

    ProjectCoreTimeSetting save(ProjectCoreTimeSetting setting);

    Optional<ProjectCoreTimeSetting> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
