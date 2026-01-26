package com.prism.statistics.presentation.project;

import static com.prism.statistics.docs.RestDocsConfiguration.field;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.project.ProjectService;
import com.prism.statistics.application.project.dto.request.CreateProjectRequest;
import com.prism.statistics.application.project.dto.response.CreateProjectResponse;
import com.prism.statistics.application.project.dto.response.ProjectListResponse;
import com.prism.statistics.application.project.dto.response.ProjectResponse;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class ProjectControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private ProjectService projectService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 프로젝트_생성_성공_테스트() throws Exception {
        // given
        CreateProjectRequest request = new CreateProjectRequest("프로젝트-이름");
        CreateProjectResponse response = new CreateProjectResponse("api-key-123");

        given(projectService.create(7L, request)).willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                post("/projects")
                        .header("Authorization", "Bearer access-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.apiKey").value("api-key-123"));

        프로젝트_생성_문서화(resultActions);
    }

    private void 프로젝트_생성_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        requestFields(
                                fieldWithPath("name").description("생성할 프로젝트 이름")
                                                     .attributes(field("constraints", "빈 값은 허용하지 않음"))
                        ),
                        responseFields(
                                fieldWithPath("apiKey").description("생성된 프로젝트의 API 키")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_프로젝트를_생성할_수_없다() throws Exception {
        // given
        CreateProjectRequest request = new CreateProjectRequest("프로젝트-이름");

        // when & then
        mockMvc.perform(
                        post("/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 프로젝트_이름이_비어_있으면_생성할_수_없다() throws Exception {
        // given
        CreateProjectRequest request = new CreateProjectRequest("");

        // when & then
        mockMvc.perform(
                        post("/projects")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("프로젝트 이름은 비어 있을 수 없습니다."));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 프로젝트_목록_조회_성공_테스트() throws Exception {
        // given
        ProjectListResponse response = new ProjectListResponse(
                List.of(
                        new ProjectResponse(1L, "프로젝트 1"),
                        new ProjectResponse(2L, "프로젝트 2")
                )
        );

        given(projectService.findByUserId(7L)).willReturn(response);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/projects")
                        .header("Authorization", "Bearer access-token")
        ).andExpect(status().isOk());

        // then
        resultActions
                .andExpect(jsonPath("$.projects[0].id").value(1L))
                .andExpect(jsonPath("$.projects[0].name").value("프로젝트 1"))
                .andExpect(jsonPath("$.projects[1].id").value(2L))
                .andExpect(jsonPath("$.projects[1].name").value("프로젝트 2"));

        프로젝트_목록_조회_문서화(resultActions);
    }

    private void 프로젝트_목록_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        responseFields(
                                fieldWithPath("projects").description("프로젝트 목록"),
                                fieldWithPath("projects[].id").description("프로젝트 ID"),
                                fieldWithPath("projects[].name").description("프로젝트 이름")
                        )
                )
        );
    }
}
