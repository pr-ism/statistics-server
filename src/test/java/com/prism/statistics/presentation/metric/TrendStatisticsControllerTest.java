package com.prism.statistics.presentation.metric;

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

import com.prism.statistics.application.metric.TrendStatisticsQueryService;
import com.prism.statistics.application.metric.dto.request.TrendStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.TrendStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.TrendStatisticsResponse.TrendDataPoint;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class TrendStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private TrendStatisticsQueryService trendStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void PR_생성_트렌드_조회_성공_테스트() throws Exception {
        // given
        TrendStatisticsResponse response = new TrendStatisticsResponse(
                "WEEKLY",
                List.of(
                        new TrendDataPoint(LocalDate.of(2024, 1, 1), 12L, 350.5),
                        new TrendDataPoint(LocalDate.of(2024, 1, 8), 0L, 0.0),
                        new TrendDataPoint(LocalDate.of(2024, 1, 15), 8L, 1500.0)
                )
        );

        given(trendStatisticsQueryService.findTrendStatistics(eq(7L), eq(1L), any(TrendStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/trends", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("period", "WEEKLY")
                                .queryParam("startDate", "2024-01-01")
                                .queryParam("endDate", "2024-03-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period").value("WEEKLY"))
                .andExpect(jsonPath("$.trends").isArray())
                .andExpect(jsonPath("$.trends.length()").value(3))
                .andExpect(jsonPath("$.trends[0].periodStart").value("2024-01-01"))
                .andExpect(jsonPath("$.trends[0].prCount").value(12))
                .andExpect(jsonPath("$.trends[0].averageChangeAmount").value(350.5))
                .andExpect(jsonPath("$.trends[1].periodStart").value("2024-01-08"))
                .andExpect(jsonPath("$.trends[1].prCount").value(0))
                .andExpect(jsonPath("$.trends[1].averageChangeAmount").value(0.0))
                .andExpect(jsonPath("$.trends[2].periodStart").value("2024-01-15"))
                .andExpect(jsonPath("$.trends[2].prCount").value(8))
                .andExpect(jsonPath("$.trends[2].averageChangeAmount").value(1500.0));

        PR_생성_트렌드_조회_문서화(resultActions);
    }

    private void PR_생성_트렌드_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        queryParameters(
                                parameterWithName("period").description("기간 단위 (WEEKLY, MONTHLY)")
                                        .attributes(field("constraints", "필수")),
                                parameterWithName("startDate").description("조회 시작 날짜")
                                        .attributes(field("constraints", "YYYY-MM-DD 포맷, 필수")),
                                parameterWithName("endDate").description("조회 종료 날짜")
                                        .attributes(field("constraints", "YYYY-MM-DD 포맷, 필수"))
                        ),
                        responseFields(
                                fieldWithPath("period").description("기간 단위 (WEEKLY, MONTHLY)"),
                                fieldWithPath("trends").description("트렌드 데이터 목록"),
                                fieldWithPath("trends[].periodStart").description("기간 시작일"),
                                fieldWithPath("trends[].prCount").description("해당 기간의 PR 생성 수"),
                                fieldWithPath("trends[].averageChangeAmount").description("해당 기간의 평균 코드 변경량 (additions + deletions)")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_트렌드_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/trends", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 존재하지_않는_프로젝트의_트렌드_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(trendStatisticsQueryService.findTrendStatistics(eq(7L), eq(999L), any(TrendStatisticsRequest.class)))
                .willThrow(new ProjectNotFoundException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/trends", 999L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("period", "WEEKLY")
                                .queryParam("startDate", "2024-01-01")
                                .queryParam("endDate", "2024-03-01")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P00"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 시작일이_종료일보다_늦으면_400을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/trends", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("period", "WEEKLY")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void period가_없으면_400을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/trends", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-01-01")
                                .queryParam("endDate", "2024-03-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"));
    }
}
