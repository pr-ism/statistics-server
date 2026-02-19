package com.prism.statistics.application.project;

import com.prism.statistics.application.project.dto.request.CreateProjectRequest;
import com.prism.statistics.application.project.dto.response.CreateProjectResponse;
import com.prism.statistics.application.project.dto.response.ProjectListResponse;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.ProjectApiKeyGenerator;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.setting.ProjectCoreTimeSetting;
import com.prism.statistics.domain.project.setting.ProjectSizeGradeThresholdSetting;
import com.prism.statistics.domain.project.setting.ProjectSizeWeightSetting;
import com.prism.statistics.domain.project.setting.repository.ProjectCoreTimeSettingRepository;
import com.prism.statistics.domain.project.setting.repository.ProjectSizeGradeThresholdSettingRepository;
import com.prism.statistics.domain.project.setting.repository.ProjectSizeWeightSettingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectApiKeyGenerator projectApiKeyGenerator;
    private final ProjectCoreTimeSettingRepository projectCoreTimeSettingRepository;
    private final ProjectSizeWeightSettingRepository projectSizeWeightSettingRepository;
    private final ProjectSizeGradeThresholdSettingRepository projectSizeGradeThresholdSettingRepository;

    @Transactional
    public CreateProjectResponse create(Long userId, CreateProjectRequest request) {
        String apiKey = projectApiKeyGenerator.generate();
        Project project = Project.create(request.name(), apiKey, userId);
        Project savedProject = projectRepository.save(project);

        projectCoreTimeSettingRepository.save(ProjectCoreTimeSetting.createDefault(savedProject.getId()));
        projectSizeWeightSettingRepository.save(ProjectSizeWeightSetting.createDefault(savedProject.getId()));
        projectSizeGradeThresholdSettingRepository.save(ProjectSizeGradeThresholdSetting.createDefault(savedProject.getId()));

        return CreateProjectResponse.from(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectListResponse find(Long userId) {
        List<ProjectListResponse.ProjectResponse> projects = projectRepository.findAllProjectsByUserId(userId).stream()
                .map(project -> ProjectListResponse.ProjectResponse.from(project))
                .toList();

        return new ProjectListResponse(projects);
    }
}
