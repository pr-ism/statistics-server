package com.prism.statistics.presentation.statistics;

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
class WeeklyTrendStatisticsControllerTest extends CommonControllerSliceTestSupport {

    private static final long USER_ID = 7L;
    private static final long PROJECT_ID = 1L;
    private static final long OTHER_PROJECT_ID = 999L;
    private static final long MERGED_COUNT_10 = 10L;
    private static final long MERGED_COUNT_8 = 8L;
    private static final long MERGED_COUNT_12 = 12L;
    private static final long CLOSED_COUNT_2 = 2L;
    private static final long CLOSED_COUNT_1 = 1L;
    private static final long CLOSED_COUNT_3 = 3L;
    private static final long MONTHLY_MERGED_30 = 30L;
    private static final long MONTHLY_CLOSED_6 = 6L;
    private static final long MONTHLY_MERGED_25 = 25L;
    private static final long MONTHLY_CLOSED_4 = 4L;
    private static final double REVIEW_WAIT_120_5 = 120.5;
    private static final double REVIEW_WAIT_95_0 = 95.0;
    private static final double REVIEW_WAIT_85_75 = 85.75;
    private static final double SIZE_SCORE_150_5 = 150.5;
    private static final double SIZE_SCORE_120_0 = 120.0;
    private static final double SIZE_SCORE_180_25 = 180.25;
    private static final String ACCESS_TOKEN = "Bearer access-token";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String START_DATE = "2026-01-01";
    private static final String END_DATE = "2026-02-28";
    private static final String INVALID_START_DATE = "2024-02-01";
    private static final String INVALID_END_DATE = "2024-01-01";
    private static final String ERROR_CODE_FORBIDDEN = "A04";
    private static final String ERROR_CODE_PROJECT_NOT_FOUND = "P00";
    private static final String ERROR_CODE_INVALID_DATE = "D03";
    private static final String MESSAGE_UNAUTHORIZED = "인가되지 않은 회원";
    private static final String MESSAGE_INVALID_DATE_RANGE = "시작일과 종료일은 둘 다 입력하거나 둘 다 생략해야 하며, 종료일은 시작일보다 빠를 수 없습니다.";
    private static final LocalDate WEEK_START_1 = LocalDate.of(2026, 1, 6);
    private static final LocalDate WEEK_START_2 = LocalDate.of(2026, 1, 13);
    private static final LocalDate WEEK_START_3 = LocalDate.of(2026, 1, 20);
    private static final int YEAR_2026 = 2026;
    private static final int MONTH_1 = 1;
    private static final int MONTH_2 = 2;

    @Autowired
    private WeeklyTrendStatisticsQueryService weeklyTrendStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 주간_추이_통계_조회_성공_테스트() throws Exception {
        // given
        List<WeeklyThroughput> weeklyThroughput = List.of(
                WeeklyThroughput.of(WEEK_START_1, MERGED_COUNT_10, CLOSED_COUNT_2),
                WeeklyThroughput.of(WEEK_START_2, MERGED_COUNT_8, CLOSED_COUNT_1),
                WeeklyThroughput.of(WEEK_START_3, MERGED_COUNT_12, CLOSED_COUNT_3)
        );

        List<MonthlyThroughput> monthlyThroughput = List.of(
                MonthlyThroughput.of(YEAR_2026, MONTH_1, MONTHLY_MERGED_30, MONTHLY_CLOSED_6),
                MonthlyThroughput.of(YEAR_2026, MONTH_2, MONTHLY_MERGED_25, MONTHLY_CLOSED_4)
        );

        List<WeeklyReviewWaitTime> weeklyReviewWaitTimeTrend = List.of(
                WeeklyReviewWaitTime.of(WEEK_START_1, REVIEW_WAIT_120_5),
                WeeklyReviewWaitTime.of(WEEK_START_2, REVIEW_WAIT_95_0),
                WeeklyReviewWaitTime.of(WEEK_START_3, REVIEW_WAIT_85_75)
        );

        List<WeeklyPrSize> weeklyPrSizeTrend = List.of(
                WeeklyPrSize.of(WEEK_START_1, SIZE_SCORE_150_5),
                WeeklyPrSize.of(WEEK_START_2, SIZE_SCORE_120_0),
                WeeklyPrSize.of(WEEK_START_3, SIZE_SCORE_180_25)
        );

        WeeklyTrendStatisticsResponse response = new WeeklyTrendStatisticsResponse(
                weeklyThroughput,
                monthlyThroughput,
                weeklyReviewWaitTimeTrend,
                weeklyPrSizeTrend
        );

        given(weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(eq(USER_ID), eq(PROJECT_ID), any(WeeklyTrendStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/weekly-trend", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                                .queryParam("startDate", START_DATE)
                                .queryParam("endDate", END_DATE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeklyThroughput").isArray())
                .andExpect(jsonPath("$.weeklyThroughput[0].weekStartDate").value(WEEK_START_1.toString()))
                .andExpect(jsonPath("$.weeklyThroughput[0].mergedCount").value(MERGED_COUNT_10))
                .andExpect(jsonPath("$.weeklyThroughput[0].closedCount").value(CLOSED_COUNT_2))
                .andExpect(jsonPath("$.monthlyThroughput").isArray())
                .andExpect(jsonPath("$.monthlyThroughput[0].year").value(YEAR_2026))
                .andExpect(jsonPath("$.monthlyThroughput[0].month").value(MONTH_1))
                .andExpect(jsonPath("$.monthlyThroughput[0].mergedCount").value(MONTHLY_MERGED_30))
                .andExpect(jsonPath("$.monthlyThroughput[0].closedCount").value(MONTHLY_CLOSED_6))
                .andExpect(jsonPath("$.weeklyReviewWaitTimeTrend").isArray())
                .andExpect(jsonPath("$.weeklyReviewWaitTimeTrend[0].weekStartDate").value(WEEK_START_1.toString()))
                .andExpect(jsonPath("$.weeklyReviewWaitTimeTrend[0].avgReviewWaitTimeMinutes").value(REVIEW_WAIT_120_5))
                .andExpect(jsonPath("$.weeklyPrSizeTrend").isArray())
                .andExpect(jsonPath("$.weeklyPrSizeTrend[0].weekStartDate").value(WEEK_START_1.toString()))
                .andExpect(jsonPath("$.weeklyPrSizeTrend[0].avgSizeScore").value(SIZE_SCORE_150_5));

        주간_추이_통계_조회_문서화(resultActions);
    }

    private void 주간_추이_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
        given(weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(eq(USER_ID), eq(PROJECT_ID), any(WeeklyTrendStatisticsRequest.class)))
                .willReturn(WeeklyTrendStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/weekly-trend", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
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
                        get("/projects/{projectId}/statistics/weekly-trend", PROJECT_ID)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_FORBIDDEN))
                .andExpect(jsonPath("$.message").value(MESSAGE_UNAUTHORIZED));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(weeklyTrendStatisticsQueryService.findWeeklyTrendStatistics(eq(USER_ID), eq(OTHER_PROJECT_ID), any(WeeklyTrendStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/weekly-trend", OTHER_PROJECT_ID)
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
                        get("/projects/{projectId}/statistics/weekly-trend", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                                .queryParam("startDate", INVALID_START_DATE)
                                .queryParam("endDate", INVALID_END_DATE)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_INVALID_DATE))
                .andExpect(jsonPath("$.message").value(MESSAGE_INVALID_DATE_RANGE));
    }
}
