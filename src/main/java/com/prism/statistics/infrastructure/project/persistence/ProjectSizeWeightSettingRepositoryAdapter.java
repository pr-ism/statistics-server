package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.setting.ProjectSizeWeightSetting;
import com.prism.statistics.domain.project.setting.repository.ProjectSizeWeightSettingRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProjectSizeWeightSettingRepositoryAdapter implements ProjectSizeWeightSettingRepository {

    private final JpaProjectSizeWeightSettingRepository jpaProjectSizeWeightSettingRepository;

    @Override
    public ProjectSizeWeightSetting save(ProjectSizeWeightSetting setting) {
        return jpaProjectSizeWeightSettingRepository.save(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectSizeWeightSetting> findByProjectId(Long projectId) {
        return jpaProjectSizeWeightSettingRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProjectId(Long projectId) {
        return jpaProjectSizeWeightSettingRepository.existsByProjectId(projectId);
    }
}
