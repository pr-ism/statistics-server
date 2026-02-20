package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.setting.ProjectSizeGradeThresholdSetting;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

public interface JpaProjectSizeGradeThresholdSettingRepository extends ListCrudRepository<ProjectSizeGradeThresholdSetting, Long> {

    Optional<ProjectSizeGradeThresholdSetting> findByProjectId(Long projectId);

    boolean existsByProjectId(Long projectId);
}
