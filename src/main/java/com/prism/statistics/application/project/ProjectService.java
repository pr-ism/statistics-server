package com.prism.statistics.application.project;

import com.prism.statistics.application.project.dto.request.CreateProjectRequest;
import com.prism.statistics.application.project.dto.response.CreateProjectResponse;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.ProjectApiKeyGenerator;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectApiKeyGenerator projectApiKeyGenerator;

    public CreateProjectResponse create(Long userId, CreateProjectRequest request) {
        String apiKey = projectApiKeyGenerator.generate();
        Project project = Project.create(request.name(), apiKey, userId);
        Project savedProject = projectRepository.save(project);

        return CreateProjectResponse.from(savedProject);
    }
}
