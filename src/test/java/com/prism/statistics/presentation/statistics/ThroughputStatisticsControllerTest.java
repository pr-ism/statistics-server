package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.ThroughputStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.ThroughputStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ThroughputStatisticsResponse;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import static com.prism.statistics.docs.RestDocsConfiguration.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
class ThroughputStatisticsControllerTest extends CommonControllerSliceTestSupport {

    private static final long USER_ID = 7L;
    private static final long PROJECT_ID = 1L;
    private static final long OTHER_PROJECT_ID = 999L;
    private static final long MERGED_PR_COUNT = 85L;
    private static final long CLOSED_PR_COUNT = 15L;
    private static final double AVG_MERGE_TIME_MINUTES = 1440.5;
    private static final double MERGE_SUCCESS_RATE = 85.0;
    private static final double CLOSED_PR_RATE = 15.0;
    private static final long ZERO_LONG = 0L;
    private static final double ZERO_DOUBLE = 0.0;
    private static final String ACCESS_TOKEN = "Bearer access-token";
    private static final String START_DATE = "2026-01-01";
    private static final String END_DATE = "2026-02-01";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String INVALID_START_DATE = "2024-02-01";
    private static final String INVALID_END_DATE = "2024-01-01";
    private static final String ERROR_CODE_FORBIDDEN = "A04";
    private static final String ERROR_CODE_PROJECT_NOT_FOUND = "P00";
    private static final String ERROR_CODE_INVALID_DATE = "D03";
    private static final String MESSAGE_UNAUTHORIZED = "인가되지 않은 회원";
    private static final String MESSAGE_INVALID_DATE_RANGE = "종료일은 시작일보다 빠를 수 없습니다.";

    @Autowired
    private ThroughputStatisticsQueryService throughputStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 처리량_통계_조회_성공_테스트() throws Exception {
        // given
        ThroughputStatisticsResponse response = ThroughputStatisticsResponse.of(
                MERGED_PR_COUNT,
                CLOSED_PR_COUNT,
                AVG_MERGE_TIME_MINUTES,
                MERGE_SUCCESS_RATE,
                CLOSED_PR_RATE
        );

        given(throughputStatisticsQueryService.findThroughputStatistics(eq(USER_ID), eq(PROJECT_ID), any(ThroughputStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/throughput", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                                .queryParam("startDate", START_DATE)
                                .queryParam("endDate", END_DATE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mergedPrCount").value(MERGED_PR_COUNT))
                .andExpect(jsonPath("$.closedPrCount").value(CLOSED_PR_COUNT))
                .andExpect(jsonPath("$.avgMergeTimeMinutes").value(AVG_MERGE_TIME_MINUTES))
                .andExpect(jsonPath("$.mergeSuccessRate").value(MERGE_SUCCESS_RATE))
                .andExpect(jsonPath("$.closedPrRate").value(CLOSED_PR_RATE));

        처리량_통계_조회_문서화(resultActions);
    }

    private void 처리량_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("mergedPrCount").description("Merge된 PR 수"),
                                fieldWithPath("closedPrCount").description("Close된 PR 수 (Merge 제외)"),
                                fieldWithPath("avgMergeTimeMinutes").description("평균 Merge 소요 시간 (분)"),
                                fieldWithPath("mergeSuccessRate").description("Merge 성공률 (%, Merged / (Merged + Closed))"),
                                fieldWithPath("closedPrRate").description("PR 폐기율 (%, Closed / (Merged + Closed))")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(throughputStatisticsQueryService.findThroughputStatistics(eq(USER_ID), eq(PROJECT_ID), any(ThroughputStatisticsRequest.class)))
                .willReturn(ThroughputStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/throughput", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mergedPrCount").value(ZERO_LONG))
                .andExpect(jsonPath("$.closedPrCount").value(ZERO_LONG))
                .andExpect(jsonPath("$.avgMergeTimeMinutes").value(ZERO_DOUBLE))
                .andExpect(jsonPath("$.mergeSuccessRate").value(ZERO_DOUBLE))
                .andExpect(jsonPath("$.closedPrRate").value(ZERO_DOUBLE));
    }

    @Test
    void 인증_정보가_없으면_처리량_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/throughput", PROJECT_ID)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_FORBIDDEN))
                .andExpect(jsonPath("$.message").value(MESSAGE_UNAUTHORIZED));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(throughputStatisticsQueryService.findThroughputStatistics(eq(USER_ID), eq(OTHER_PROJECT_ID), any(ThroughputStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/throughput", OTHER_PROJECT_ID)
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
                        get("/projects/{projectId}/statistics/throughput", PROJECT_ID)
                                .header(HEADER_AUTHORIZATION, ACCESS_TOKEN)
                                .queryParam("startDate", INVALID_START_DATE)
                                .queryParam("endDate", INVALID_END_DATE)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_INVALID_DATE))
                .andExpect(jsonPath("$.message").value(MESSAGE_INVALID_DATE_RANGE));
    }
}
