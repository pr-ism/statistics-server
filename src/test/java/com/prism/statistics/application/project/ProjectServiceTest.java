package com.prism.statistics.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.project.dto.request.CreateProjectRequest;
import com.prism.statistics.application.project.dto.response.CreateProjectResponse;
import com.prism.statistics.application.project.dto.response.ProjectListResponse;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void api_key가_발급된_프로젝트를_생성한다() {
        // given
        CreateProjectRequest request = new CreateProjectRequest("통계-프로젝트");

        // when
        CreateProjectResponse actual = projectService.create(1L, request);

        // then
        assertThat(actual.apiKey()).isNotBlank();
    }

    @Test
    void 사용자_ID로_프로젝트_목록을_조회한다() {
        // given
        Long userId = 7L;
        Long otherUserId = 8L;

        projectRepository.save(Project.create("프로젝트 1", "api-key-1", userId));
        projectRepository.save(Project.create("프로젝트 2", "api-key-2", userId));
        projectRepository.save(Project.create("다른 사용자 프로젝트", "api-key-3", otherUserId));

        // when
        ProjectListResponse actual = projectService.findByUserId(userId);

        // then
        assertThat(actual.getProjects()).hasSize(2)
                                        .extracting("name")
                                        .containsExactlyInAnyOrder(
                                                "프로젝트 1",
                                                "프로젝트 2"
                                        );
    }
}
