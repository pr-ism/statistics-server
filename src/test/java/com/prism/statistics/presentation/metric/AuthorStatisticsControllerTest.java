package com.prism.statistics.presentation.metric;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.metric.AuthorStatisticsQueryService;
import com.prism.statistics.application.metric.dto.response.AuthorStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.AuthorStatisticsResponse.AuthorStatistics;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class AuthorStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private AuthorStatisticsQueryService authorStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 작성자별_통계_조회_성공_테스트() throws Exception {
        // given
        AuthorStatisticsResponse response = new AuthorStatisticsResponse(
                List.of(
                        new AuthorStatistics("author1", 5, 500, 200, 100.0, 40.0, 3.0, 4.0),
                        new AuthorStatistics("author2", 3, 300, 100, 100.0, 33.3, 2.0, 5.0)
                )
        );

        given(authorStatisticsQueryService.findAuthorStatistics(7L, 1L)).willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/authors", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.authorStatistics").isArray())
                .andExpect(jsonPath("$.authorStatistics.length()").value(2))
                .andExpect(jsonPath("$.authorStatistics[0].authorGithubId").value("author1"))
                .andExpect(jsonPath("$.authorStatistics[0].pullRequestCount").value(5))
                .andExpect(jsonPath("$.authorStatistics[0].totalAdditions").value(500))
                .andExpect(jsonPath("$.authorStatistics[0].totalDeletions").value(200))
                .andExpect(jsonPath("$.authorStatistics[0].averageAdditions").value(100.0))
                .andExpect(jsonPath("$.authorStatistics[0].averageDeletions").value(40.0))
                .andExpect(jsonPath("$.authorStatistics[0].averageCommitCount").value(3.0))
                .andExpect(jsonPath("$.authorStatistics[0].averageChangedFileCount").value(4.0));

        작성자별_통계_조회_문서화(resultActions);
    }

    private void 작성자별_통계_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        responseFields(
                                fieldWithPath("authorStatistics").description("작성자별 통계 목록"),
                                fieldWithPath("authorStatistics[].authorGithubId").description("작성자 GitHub ID"),
                                fieldWithPath("authorStatistics[].pullRequestCount").description("PR 수"),
                                fieldWithPath("authorStatistics[].totalAdditions").description("총 추가 라인 수"),
                                fieldWithPath("authorStatistics[].totalDeletions").description("총 삭제 라인 수"),
                                fieldWithPath("authorStatistics[].averageAdditions").description("평균 추가 라인 수"),
                                fieldWithPath("authorStatistics[].averageDeletions").description("평균 삭제 라인 수"),
                                fieldWithPath("authorStatistics[].averageCommitCount").description("평균 커밋 수"),
                                fieldWithPath("authorStatistics[].averageChangedFileCount").description("평균 변경 파일 수")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_작성자별_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/authors", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 존재하지_않는_프로젝트의_작성자별_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(authorStatisticsQueryService.findAuthorStatistics(7L, 999L))
                .willThrow(new ProjectNotFoundException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/authors", 999L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P00"));
    }
}
