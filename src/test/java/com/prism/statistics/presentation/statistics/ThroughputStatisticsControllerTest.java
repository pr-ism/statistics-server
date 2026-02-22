package com.prism.statistics.presentation.statistics;

import static com.prism.statistics.docs.RestDocsConfiguration.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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

import com.prism.statistics.application.statistics.ThroughputStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.ThroughputStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ThroughputStatisticsResponse;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class ThroughputStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private ThroughputStatisticsQueryService throughputStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 처리량_통계_조회_성공_테스트() throws Exception {
        // given
        ThroughputStatisticsResponse response = ThroughputStatisticsResponse.of(
                85L,
                15L,
                1440.5,
                85.0
        );

        given(throughputStatisticsQueryService.findThroughputStatistics(eq(7L), eq(1L), any(ThroughputStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/throughput", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mergedPrCount").value(85))
                .andExpect(jsonPath("$.closedPrCount").value(15))
                .andExpect(jsonPath("$.avgMergeTimeMinutes").value(1440.5))
                .andExpect(jsonPath("$.mergeSuccessRate").value(85.0));

        처리량_통계_조회_문서화(resultActions);
    }

    private void 처리량_통계_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        queryParameters(
                                parameterWithName("startDate").description("조회 시작 날짜")
                                        .attributes(field("constraints", "YYYY-MM-DD 포맷")).optional(),
                                parameterWithName("endDate").description("조회 종료 날짜")
                                        .attributes(field("constraints", "YYYY-MM-DD 포맷")).optional()
                        ),
                        responseFields(
                                fieldWithPath("mergedPrCount").description("Merge된 PR 수"),
                                fieldWithPath("closedPrCount").description("Close된 PR 수 (Merge 제외)"),
                                fieldWithPath("avgMergeTimeMinutes").description("평균 Merge 소요 시간 (분)"),
                                fieldWithPath("mergeSuccessRate").description("Merge 성공률 (%, Merged / (Merged + Closed))")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(throughputStatisticsQueryService.findThroughputStatistics(eq(7L), eq(1L), any(ThroughputStatisticsRequest.class)))
                .willReturn(ThroughputStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/throughput", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mergedPrCount").value(0))
                .andExpect(jsonPath("$.closedPrCount").value(0))
                .andExpect(jsonPath("$.avgMergeTimeMinutes").value(0.0))
                .andExpect(jsonPath("$.mergeSuccessRate").value(0.0));
    }

    @Test
    void 인증_정보가_없으면_처리량_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/throughput", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(throughputStatisticsQueryService.findThroughputStatistics(eq(7L), eq(999L), any(ThroughputStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/throughput", 999L)
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
                        get("/projects/{projectId}/statistics/throughput", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
