package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.DailyTrendStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.DailyTrendStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse.DailyPrTrend;
import com.prism.statistics.application.statistics.dto.response.DailyTrendStatisticsResponse.TrendSummary;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDate;
import java.util.List;

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

@SuppressWarnings("NonAsciiCharacters")
class DailyTrendStatisticsControllerTest extends CommonControllerSliceTestSupport {

    private static final long USER_ID = 7L;
    private static final long PROJECT_ID = 1L;
    private static final long OTHER_PROJECT_ID = 999L;
    private static final long CREATED_COUNT_1 = 5L;
    private static final long CREATED_COUNT_2 = 8L;
    private static final long CREATED_COUNT_3 = 3L;
    private static final long MERGED_COUNT_1 = 4L;
    private static final long MERGED_COUNT_2 = 6L;
    private static final long MERGED_COUNT_3 = 2L;
    private static final long TOTAL_CREATED_COUNT = 16L;
    private static final long TOTAL_MERGED_COUNT = 12L;
    private static final long PEAK_CREATED_COUNT = 8L;
    private static final long PEAK_MERGED_COUNT = 6L;
    private static final long ZERO_LONG = 0L;
    private static final double AVG_CREATED_COUNT = 5.33;
    private static final double AVG_MERGED_COUNT = 4.0;
    private static final String ACCESS_TOKEN = "Bearer access-token";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String START_DATE = "2026-01-01";
    private static final String END_DATE = "2026-01-31";
    private static final String INVALID_START_DATE = "2024-02-01";
    private static final String INVALID_END_DATE = "2024-01-01";
    private static final String ERROR_CODE_FORBIDDEN = "A04";
    private static final String ERROR_CODE_PROJECT_NOT_FOUND = "P00";
    private static final String ERROR_CODE_INVALID_DATE = "D03";
    private static final String MESSAGE_UNAUTHORIZED = "인가되지 않은 회원";
    private static final String MESSAGE_INVALID_DATE_RANGE = "종료일은 시작일보다 빠를 수 없습니다.";
    private static final LocalDate CREATED_DATE_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate CREATED_DATE_2 = LocalDate.of(2026, 1, 2);
    private static final LocalDate CREATED_DATE_3 = LocalDate.of(2026, 1, 3);
    private static final LocalDate PEAK_DATE = LocalDate.of(2026, 1, 2);

    @Autowired
    private DailyTrendStatisticsQueryService dailyTrendStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 일별_추이_통계_조회_성공_테스트() throws Exception {
        // given
        List<DailyPrTrend> dailyCreatedTrend = List.of(
                DailyPrTrend.of(CREATED_DATE_1, CREATED_COUNT_1),
                DailyPrTrend.of(CREATED_DATE_2, CREATED_COUNT_2),
                DailyPrTrend.of(CREATED_DATE_3, CREATED_COUNT_3)
        );

        List<DailyPrTrend> dailyMergedTrend = List.of(
                DailyPrTrend.of(CREATED_DATE_1, MERGED_COUNT_1),
                DailyPrTrend.of(CREATED_DATE_2, MERGED_COUNT_2),
                DailyPrTrend.of(CREATED_DATE_3, MERGED_COUNT_3)
        );

        TrendSummary summary = TrendSummary.of(
                TOTAL_CREATED_COUNT,
                TOTAL_MERGED_COUNT,
                AVG_CREATED_COUNT,
                AVG_MERGED_COUNT,
                PEAK_DATE,
                PEAK_CREATED_COUNT,
                PEAK_DATE,
                PEAK_MERGED_COUNT
        );

        DailyTrendStatisticsResponse response = new DailyTrendStatisticsResponse(
                dailyCreatedTrend,
                dailyMergedTrend,
                summary
        );

        given(dailyTrendStatisticsQueryService.findDailyTrendStatistics(eq(USER_ID), eq(PROJECT_ID), any(DailyTrendStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                                .queryParam("startDate", START_DATE)
                                .queryParam("endDate", END_DATE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCreatedTrend").isArray())
                .andExpect(jsonPath("$.dailyCreatedTrend[0].date").value(CREATED_DATE_1.toString()))
                .andExpect(jsonPath("$.dailyCreatedTrend[0].count").value(CREATED_COUNT_1))
                .andExpect(jsonPath("$.dailyMergedTrend").isArray())
                .andExpect(jsonPath("$.dailyMergedTrend[0].date").value(CREATED_DATE_1.toString()))
                .andExpect(jsonPath("$.dailyMergedTrend[0].count").value(MERGED_COUNT_1))
                .andExpect(jsonPath("$.summary.totalCreatedCount").value(TOTAL_CREATED_COUNT))
                .andExpect(jsonPath("$.summary.totalMergedCount").value(TOTAL_MERGED_COUNT))
                .andExpect(jsonPath("$.summary.avgDailyCreatedCount").value(AVG_CREATED_COUNT))
                .andExpect(jsonPath("$.summary.avgDailyMergedCount").value(AVG_MERGED_COUNT))
                .andExpect(jsonPath("$.summary.peakCreatedDate").value(PEAK_DATE.toString()))
                .andExpect(jsonPath("$.summary.peakCreatedCount").value(PEAK_CREATED_COUNT))
                .andExpect(jsonPath("$.summary.peakMergedDate").value(PEAK_DATE.toString()))
                .andExpect(jsonPath("$.summary.peakMergedCount").value(PEAK_MERGED_COUNT));

        일별_추이_통계_조회_문서화(resultActions);
    }

    private void 일별_추이_통계_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName(HEADER_AUTHORIZATION).description("Access Token 값")
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
        given(dailyTrendStatisticsQueryService.findDailyTrendStatistics(eq(USER_ID), eq(PROJECT_ID), any(DailyTrendStatisticsRequest.class)))
                .willReturn(DailyTrendStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyCreatedTrend").isEmpty())
                .andExpect(jsonPath("$.dailyMergedTrend").isEmpty())
                .andExpect(jsonPath("$.summary.totalCreatedCount").value(ZERO_LONG))
                .andExpect(jsonPath("$.summary.totalMergedCount").value(ZERO_LONG));
    }

    @Test
    void 인증_정보가_없으면_일별_추이_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", PROJECT_ID)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_FORBIDDEN))
                .andExpect(jsonPath("$.message").value(MESSAGE_UNAUTHORIZED));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(dailyTrendStatisticsQueryService.findDailyTrendStatistics(eq(USER_ID), eq(OTHER_PROJECT_ID), any(DailyTrendStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", OTHER_PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_PROJECT_NOT_FOUND));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 시작일이_종료일보다_늦으면_400을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/daily-trend", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                                .queryParam("startDate", INVALID_START_DATE)
                                .queryParam("endDate", INVALID_END_DATE)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_INVALID_DATE))
                .andExpect(jsonPath("$.message").value(MESSAGE_INVALID_DATE_RANGE));
    }
}
