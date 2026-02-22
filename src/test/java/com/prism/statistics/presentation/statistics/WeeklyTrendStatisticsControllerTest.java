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

import com.prism.statistics.application.statistics.WeeklyTrendStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.WeeklyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse.MonthlyThroughput;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse.WeeklyPrSize;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse.WeeklyReviewWaitTime;
import com.prism.statistics.application.statistics.dto.response.WeeklyTrendStatisticsResponse.WeeklyThroughput;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class WeeklyTrendStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private WeeklyTrendStatisticsQueryService weeklyTrendStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 주간_추이_통계_조회_성공_테스트() throws Exception {
        // given
        List<WeeklyThroughput> weeklyThroughput = List.of(
                WeeklyThroughput.of(LocalDate.of(2026, 1, 6), 10L, 2L),
                WeeklyThroughput.of(LocalDate.of(2026, 1, 13), 8L, 1L),
                WeeklyThroughput.of(LocalDate.of(2026, 1, 20), 12L, 3L)
        );

        List<MonthlyThroughput> monthlyThroughput = List.of(
                MonthlyThroughput.of(2026, 1, 30L, 6L),
                MonthlyThroughput.of(2026, 2, 25L, 4L)
        );

        List<WeeklyReviewWaitTime> weeklyReviewWaitTimeTrend = List.of(
                WeeklyReviewWaitTime.of(LocalDate.of(2026, 1, 6), 120.5),
                WeeklyReviewWaitTime.of(LocalDate.of(2026, 1, 13), 95.0),
                WeeklyReviewWaitTime.of(LocalDate.of(2026, 1, 20), 85.75)
        );

        List<WeeklyPrSize> weeklyPrSizeTrend = List.of(
                WeeklyPrSize.of(LocalDate.of(2026, 1, 6), 150.5),
                WeeklyPrSize.of(LocalDate.of(2026, 1, 13), 120.0),
                WeeklyPrSize.of(LocalDate.of(2026, 1, 20), 180.25)
        );

        WeeklyTrendStatisticsResponse response = new WeeklyTrendStatisticsResponse(
                weeklyThroughput,
                monthlyThroughput,
                weeklyReviewWaitTimeTrend,
                weeklyPrSizeTrend
        );

        given(weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(eq(7L), eq(1L), any(WeeklyTrendStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/weekly-trend", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-28")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyThroughput").isArray())
                .andExpect(jsonPath("$.weeklyThroughput[0].weekStartDate").value("2026-01-06"))
                .andExpect(jsonPath("$.weeklyThroughput[0].mergedCount").value(10))
                .andExpect(jsonPath("$.weeklyThroughput[0].closedCount").value(2))
                .andExpect(jsonPath("$.monthlyThroughput").isArray())
                .andExpect(jsonPath("$.monthlyThroughput[0].year").value(2026))
                .andExpect(jsonPath("$.monthlyThroughput[0].month").value(1))
                .andExpect(jsonPath("$.monthlyThroughput[0].mergedCount").value(30))
                .andExpect(jsonPath("$.monthlyThroughput[0].closedCount").value(6))
                .andExpect(jsonPath("$.weeklyReviewWaitTimeTrend").isArray())
                .andExpect(jsonPath("$.weeklyReviewWaitTimeTrend[0].weekStartDate").value("2026-01-06"))
                .andExpect(jsonPath("$.weeklyReviewWaitTimeTrend[0].avgReviewWaitTimeMinutes").value(120.5))
                .andExpect(jsonPath("$.weeklyPrSizeTrend").isArray())
                .andExpect(jsonPath("$.weeklyPrSizeTrend[0].weekStartDate").value("2026-01-06"))
                .andExpect(jsonPath("$.weeklyPrSizeTrend[0].avgSizeScore").value(150.5));

        주간_추이_통계_조회_문서화(resultActions);
    }

    private void 주간_추이_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("weeklyThroughput").description("주간 PR 처리량"),
                                fieldWithPath("weeklyThroughput[].weekStartDate").description("주 시작 날짜 (월요일)"),
                                fieldWithPath("weeklyThroughput[].mergedCount").description("해당 주 Merge된 PR 수"),
                                fieldWithPath("weeklyThroughput[].closedCount").description("해당 주 Close된 PR 수"),
                                fieldWithPath("monthlyThroughput").description("월간 PR 처리량"),
                                fieldWithPath("monthlyThroughput[].year").description("연도"),
                                fieldWithPath("monthlyThroughput[].month").description("월"),
                                fieldWithPath("monthlyThroughput[].mergedCount").description("해당 월 Merge된 PR 수"),
                                fieldWithPath("monthlyThroughput[].closedCount").description("해당 월 Close된 PR 수"),
                                fieldWithPath("weeklyReviewWaitTimeTrend").description("주간 리뷰 대기 시간 추이"),
                                fieldWithPath("weeklyReviewWaitTimeTrend[].weekStartDate").description("주 시작 날짜 (월요일)"),
                                fieldWithPath("weeklyReviewWaitTimeTrend[].avgReviewWaitTimeMinutes").description("해당 주 평균 리뷰 대기 시간 (분)"),
                                fieldWithPath("weeklyPrSizeTrend").description("주간 PR 크기 추이"),
                                fieldWithPath("weeklyPrSizeTrend[].weekStartDate").description("주 시작 날짜 (월요일)"),
                                fieldWithPath("weeklyPrSizeTrend[].avgSizeScore").description("해당 주 평균 PR 크기 점수")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(eq(7L), eq(1L), any(WeeklyTrendStatisticsRequest.class)))
                .willReturn(WeeklyTrendStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/weekly-trend", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyThroughput").isEmpty())
                .andExpect(jsonPath("$.monthlyThroughput").isEmpty())
                .andExpect(jsonPath("$.weeklyReviewWaitTimeTrend").isEmpty())
                .andExpect(jsonPath("$.weeklyPrSizeTrend").isEmpty());
    }

    @Test
    void 인증_정보가_없으면_주간_추이_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/weekly-trend", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(eq(7L), eq(999L), any(WeeklyTrendStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/weekly-trend", 999L)
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
                        get("/projects/{projectId}/statistics/weekly-trend", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
