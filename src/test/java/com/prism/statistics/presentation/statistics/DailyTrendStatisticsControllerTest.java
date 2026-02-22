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

import com.prism.statistics.application.statistics.DailyTrendStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.DailyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse.DailyPrTrend;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse.TrendSummary;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class DailyTrendStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private DailyTrendStatisticsQueryService dailyTrendStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 일별_추이_통계_조회_성공_테스트() throws Exception {
        // given
        List<DailyPrTrend> dailyCreatedTrend = List.of(
                DailyPrTrend.of(LocalDate.of(2026, 1, 1), 5L),
                DailyPrTrend.of(LocalDate.of(2026, 1, 2), 8L),
                DailyPrTrend.of(LocalDate.of(2026, 1, 3), 3L)
        );

        List<DailyPrTrend> dailyMergedTrend = List.of(
                DailyPrTrend.of(LocalDate.of(2026, 1, 1), 4L),
                DailyPrTrend.of(LocalDate.of(2026, 1, 2), 6L),
                DailyPrTrend.of(LocalDate.of(2026, 1, 3), 2L)
        );

        TrendSummary summary = TrendSummary.of(
                16L,
                12L,
                5.33,
                4.0,
                LocalDate.of(2026, 1, 2),
                8L,
                LocalDate.of(2026, 1, 2),
                6L
        );

        DailyTrendStatisticsResponse response = new DailyTrendStatisticsResponse(
                dailyCreatedTrend,
                dailyMergedTrend,
                summary
        );

        given(dailyTrendStatisticsQueryService.findDailyTrendStatistics(eq(7L), eq(1L), any(DailyTrendStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-01-31")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCreatedTrend").isArray())
                .andExpect(jsonPath("$.dailyCreatedTrend[0].date").value("2026-01-01"))
                .andExpect(jsonPath("$.dailyCreatedTrend[0].count").value(5))
                .andExpect(jsonPath("$.dailyMergedTrend").isArray())
                .andExpect(jsonPath("$.dailyMergedTrend[0].date").value("2026-01-01"))
                .andExpect(jsonPath("$.dailyMergedTrend[0].count").value(4))
                .andExpect(jsonPath("$.summary.totalCreatedCount").value(16))
                .andExpect(jsonPath("$.summary.totalMergedCount").value(12))
                .andExpect(jsonPath("$.summary.avgDailyCreatedCount").value(5.33))
                .andExpect(jsonPath("$.summary.avgDailyMergedCount").value(4.0))
                .andExpect(jsonPath("$.summary.peakCreatedDate").value("2026-01-02"))
                .andExpect(jsonPath("$.summary.peakCreatedCount").value(8))
                .andExpect(jsonPath("$.summary.peakMergedDate").value("2026-01-02"))
                .andExpect(jsonPath("$.summary.peakMergedCount").value(6));

        일별_추이_통계_조회_문서화(resultActions);
    }

    private void 일별_추이_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("dailyCreatedTrend").description("일별 PR 생성 추이"),
                                fieldWithPath("dailyCreatedTrend[].date").description("날짜"),
                                fieldWithPath("dailyCreatedTrend[].count").description("해당 날짜 PR 생성 수"),
                                fieldWithPath("dailyMergedTrend").description("일별 PR Merge 추이"),
                                fieldWithPath("dailyMergedTrend[].date").description("날짜"),
                                fieldWithPath("dailyMergedTrend[].count").description("해당 날짜 PR Merge 수"),
                                fieldWithPath("summary").description("추이 요약 정보"),
                                fieldWithPath("summary.totalCreatedCount").description("총 PR 생성 수"),
                                fieldWithPath("summary.totalMergedCount").description("총 PR Merge 수"),
                                fieldWithPath("summary.avgDailyCreatedCount").description("일평균 PR 생성 수"),
                                fieldWithPath("summary.avgDailyMergedCount").description("일평균 PR Merge 수"),
                                fieldWithPath("summary.peakCreatedDate").description("최다 PR 생성 날짜"),
                                fieldWithPath("summary.peakCreatedCount").description("최다 PR 생성 수"),
                                fieldWithPath("summary.peakMergedDate").description("최다 PR Merge 날짜"),
                                fieldWithPath("summary.peakMergedCount").description("최다 PR Merge 수")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(dailyTrendStatisticsQueryService.findDailyTrendStatistics(eq(7L), eq(1L), any(DailyTrendStatisticsRequest.class)))
                .willReturn(DailyTrendStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCreatedTrend").isEmpty())
                .andExpect(jsonPath("$.dailyMergedTrend").isEmpty())
                .andExpect(jsonPath("$.summary.totalCreatedCount").value(0))
                .andExpect(jsonPath("$.summary.totalMergedCount").value(0));
    }

    @Test
    void 인증_정보가_없으면_일별_추이_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(dailyTrendStatisticsQueryService.findDailyTrendStatistics(eq(7L), eq(999L), any(DailyTrendStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", 999L)
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
                        get("/projects/{projectId}/statistics/daily-trend", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
