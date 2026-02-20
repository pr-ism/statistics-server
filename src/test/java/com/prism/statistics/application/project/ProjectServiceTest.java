package com.prism.statistics.application.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.project.dto.request.CreateProjectRequest;
import com.prism.statistics.application.project.dto.response.CreateProjectResponse;
import com.prism.statistics.application.project.dto.response.ProjectListResponse;
import com.prism.statistics.domain.project.Project;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.setting.repository.ProjectCoreTimeSettingRepository;
import com.prism.statistics.domain.project.setting.repository.ProjectSizeGradeThresholdSettingRepository;
import com.prism.statistics.domain.project.setting.repository.ProjectSizeWeightSettingRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectServiceTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectCoreTimeSettingRepository projectCoreTimeSettingRepository;

    @Autowired
    private ProjectSizeWeightSettingRepository projectSizeWeightSettingRepository;

    @Autowired
    private ProjectSizeGradeThresholdSettingRepository projectSizeGradeThresholdSettingRepository;

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
    void 프로젝트_생성_시_기본_설정이_함께_생성된다() {
        // given
        Long userId = 1L;
        CreateProjectRequest request = new CreateProjectRequest("설정-테스트-프로젝트");

        // when
        projectService.create(userId, request);

        // then
        List<Project> projects = projectRepository.findAllProjectsByUserId(userId);
        assertThat(projects).hasSize(1);
        Long projectId = projects.get(0).getId();

        assertThat(projectCoreTimeSettingRepository.findByProjectId(projectId)).isPresent();
        assertThat(projectSizeWeightSettingRepository.findByProjectId(projectId)).isPresent();
        assertThat(projectSizeGradeThresholdSettingRepository.findByProjectId(projectId)).isPresent();
    }

    @Sql("/sql/project/insert_projects.sql")
    @Test
    void 사용자_ID로_프로젝트_목록을_조회한다() {
        // given
        Long userId = 7L;

        // when
        ProjectListResponse actual = projectService.find(userId);

        // then
        assertThat(actual.projects()).hasSize(2)
                .extracting(projectResponse -> projectResponse.name())
                .containsExactlyInAnyOrder(
                        "프로젝트 1",
                        "프로젝트 2"
                );
    }
}
