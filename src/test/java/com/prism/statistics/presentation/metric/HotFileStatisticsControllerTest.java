package com.prism.statistics.presentation.metric;

import static com.prism.statistics.docs.RestDocsConfiguration.field;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.metric.HotFileStatisticsQueryService;
import com.prism.statistics.application.metric.dto.request.HotFileStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.HotFileStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.HotFileStatisticsResponse.HotFileStatistics;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class HotFileStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private HotFileStatisticsQueryService hotFileStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 핫_파일_통계_조회_성공_테스트() throws Exception {
        // given
        HotFileStatisticsResponse response = new HotFileStatisticsResponse(
                List.of(
                        new HotFileStatistics("src/main/java/Application.java", 15, 500, 200, 12, 1, 1, 1),
                        new HotFileStatistics("src/main/java/Config.java", 8, 300, 100, 6, 2, 0, 0),
                        new HotFileStatistics("README.md", 5, 150, 30, 3, 1, 0, 1)
                )
        );

        given(hotFileStatisticsQueryService.findHotFileStatistics(eq(7L), eq(1L), any(HotFileStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/hot-files", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("limit", "10")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hotFiles").isArray())
                .andExpect(jsonPath("$.hotFiles.length()").value(3))
                .andExpect(jsonPath("$.hotFiles[0].fileName").value("src/main/java/Application.java"))
                .andExpect(jsonPath("$.hotFiles[0].changeCount").value(15))
                .andExpect(jsonPath("$.hotFiles[0].totalAdditions").value(500))
                .andExpect(jsonPath("$.hotFiles[0].totalDeletions").value(200))
                .andExpect(jsonPath("$.hotFiles[0].modifiedCount").value(12))
                .andExpect(jsonPath("$.hotFiles[0].addedCount").value(1))
                .andExpect(jsonPath("$.hotFiles[0].removedCount").value(1))
                .andExpect(jsonPath("$.hotFiles[0].renamedCount").value(1));

        핫_파일_통계_조회_문서화(resultActions);
    }

    private void 핫_파일_통계_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        queryParameters(
                                parameterWithName("limit").description("조회할 최대 파일 수")
                                        .attributes(field("constraints", "1 이상의 정수, 기본값 10")).optional(),
                                parameterWithName("startDate").description("조회 시작 날짜")
                                        .attributes(field("constraints", "YYYY-MM-DD 포맷")).optional(),
                                parameterWithName("endDate").description("조회 종료 날짜")
                                        .attributes(field("constraints", "YYYY-MM-DD 포맷")).optional()
                        ),
                        responseFields(
                                fieldWithPath("hotFiles").description("핫 파일 통계 목록"),
                                fieldWithPath("hotFiles[].fileName").description("파일 이름"),
                                fieldWithPath("hotFiles[].changeCount").description("변경 횟수"),
                                fieldWithPath("hotFiles[].totalAdditions").description("총 추가 라인 수"),
                                fieldWithPath("hotFiles[].totalDeletions").description("총 삭제 라인 수"),
                                fieldWithPath("hotFiles[].modifiedCount").description("수정 횟수"),
                                fieldWithPath("hotFiles[].addedCount").description("추가 횟수"),
                                fieldWithPath("hotFiles[].removedCount").description("삭제 횟수"),
                                fieldWithPath("hotFiles[].renamedCount").description("이름 변경 횟수")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_핫_파일_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/hot-files", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 존재하지_않는_프로젝트의_핫_파일_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(hotFileStatisticsQueryService.findHotFileStatistics(eq(7L), eq(999L), any(HotFileStatisticsRequest.class)))
                .willThrow(new ProjectNotFoundException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/hot-files", 999L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P00"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 시작일이_종료일보다_늦으면_400을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/hot-files", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void limit가_0_이하이면_400을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/hot-files", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("limit", "0")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"));
    }
}
