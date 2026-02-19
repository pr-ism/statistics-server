package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.setting.ProjectSizeGradeThresholdSetting;
import com.prism.statistics.domain.project.setting.repository.ProjectSizeGradeThresholdSettingRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProjectSizeGradeThresholdSettingRepositoryAdapter implements ProjectSizeGradeThresholdSettingRepository {

    private final JpaProjectSizeGradeThresholdSettingRepository jpaProjectSizeGradeThresholdSettingRepository;

    @Override
    @Transactional
    public ProjectSizeGradeThresholdSetting save(ProjectSizeGradeThresholdSetting setting) {
        return jpaProjectSizeGradeThresholdSettingRepository.save(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectSizeGradeThresholdSetting> findByProjectId(Long projectId) {
        return jpaProjectSizeGradeThresholdSettingRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProjectId(Long projectId) {
        return jpaProjectSizeGradeThresholdSettingRepository.existsByProjectId(projectId);
    }
}
