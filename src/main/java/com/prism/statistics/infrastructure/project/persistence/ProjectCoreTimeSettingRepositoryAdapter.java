package com.prism.statistics.infrastructure.project.persistence;

import com.prism.statistics.domain.project.setting.ProjectCoreTimeSetting;
import com.prism.statistics.domain.project.setting.repository.ProjectCoreTimeSettingRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProjectCoreTimeSettingRepositoryAdapter implements ProjectCoreTimeSettingRepository {

    private final JpaProjectCoreTimeSettingRepository jpaProjectCoreTimeSettingRepository;

    @Override
    @Transactional
    public ProjectCoreTimeSetting save(ProjectCoreTimeSetting setting) {
        return jpaProjectCoreTimeSettingRepository.save(setting);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProjectCoreTimeSetting> findByProjectId(Long projectId) {
        return jpaProjectCoreTimeSettingRepository.findByProjectId(projectId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByProjectId(Long projectId) {
        return jpaProjectCoreTimeSettingRepository.existsByProjectId(projectId);
    }
}
