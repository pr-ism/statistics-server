package com.prism.statistics.application.project;

import com.prism.statistics.application.project.dto.request.CreateProjectRequest;
import com.prism.statistics.application.project.dto.response.CreateProjectResponse;
import com.prism.statistics.application.project.dto.response.ProjectListResponse;
import com.prism.statistics.application.project.dto.response.ProjectResponse;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.ProjectApiKeyGenerator;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectApiKeyGenerator projectApiKeyGenerator;

    @Transactional
    public CreateProjectResponse create(Long userId, CreateProjectRequest request) {
        String apiKey = projectApiKeyGenerator.generate();
        Project project = Project.create(request.name(), apiKey, userId);
        Project savedProject = projectRepository.save(project);

        return CreateProjectResponse.from(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectListResponse findByUserId(final Long userId) {
        List<ProjectResponse> projects = projectRepository.findByUserId(userId).stream()
                                                          .map(ProjectResponse::from)
                                                          .toList();

        return new ProjectListResponse(projects);
    }
}
