package com.prism.statistics.application.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.project.dto.request.CreateProjectRequest;
import com.prism.statistics.application.project.dto.response.CreateProjectResponse;
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

    @Test
    void api_key가_발급된_프로젝트를_생성한다() {
        // given
        CreateProjectRequest request = new CreateProjectRequest("통계-프로젝트");

        // when
        CreateProjectResponse actual = projectService.create(1L, request);

        // then
        assertThat(actual.apiKey()).isNotBlank();
    }
}
