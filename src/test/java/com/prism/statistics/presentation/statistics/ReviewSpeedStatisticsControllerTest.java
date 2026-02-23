package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.ReviewSpeedStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.ReviewSpeedStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.MergeWaitTimeStatistics;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.ReviewCompletionStatistics;
import com.prism.statistics.application.statistics.dto.response.ReviewSpeedStatisticsResponse.ReviewWaitTimeStatistics;
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
class ReviewSpeedStatisticsControllerTest extends CommonControllerSliceTestSupport {

    private static final long USER_ID = 7L;
    private static final long PROJECT_ID = 1L;
    private static final long OTHER_PROJECT_ID = 999L;
    private static final long TOTAL_PR_COUNT = 100L;
    private static final long REVIEWED_PR_COUNT = 85L;
    private static final long MERGED_WITH_APPROVAL_COUNT = 70L;
    private static final long CORE_TIME_REVIEW_COUNT = 55L;
    private static final long SAME_DAY_REVIEW_COUNT = 38L;
    private static final double REVIEW_RATE = 85.0;
    private static final double AVG_REVIEW_WAIT_MINUTES = 120.5;
    private static final double REVIEW_WAIT_P50_MINUTES = 90.0;
    private static final double REVIEW_WAIT_P90_MINUTES = 240.0;
    private static final double AVG_MERGE_WAIT_MINUTES = 30.5;
    private static final double CORE_TIME_REVIEW_RATE = 65.0;
    private static final double SAME_DAY_REVIEW_RATE = 45.0;
    private static final int ZERO_COUNT = 0;
    private static final double ZERO_RATE = 0.0;
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_TOKEN = "Bearer access-token";
    private static final String START_DATE = "2026-01-01";
    private static final String END_DATE = "2026-02-01";
    private static final String INVALID_START_DATE = "2024-02-01";
    private static final String INVALID_END_DATE = "2024-01-01";
    private static final String ERROR_CODE_UNAUTHORIZED = "A04";
    private static final String ERROR_CODE_PROJECT_NOT_FOUND = "P00";
    private static final String ERROR_CODE_INVALID_DATE_RANGE = "D03";
    private static final String MESSAGE_UNAUTHORIZED = "인가되지 않은 회원";
    private static final String MESSAGE_INVALID_DATE_RANGE = "종료일은 시작일보다 빠를 수 없습니다.";

    @Autowired
    private ReviewSpeedStatisticsQueryService reviewSpeedStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = USER_ID)
    void 리뷰_속도_통계_조회_성공_테스트() throws Exception {
        // given
        ReviewSpeedStatisticsResponse response = new ReviewSpeedStatisticsResponse(
                TOTAL_PR_COUNT,
                REVIEWED_PR_COUNT,
                REVIEW_RATE,
                ReviewWaitTimeStatistics.of(AVG_REVIEW_WAIT_MINUTES, REVIEW_WAIT_P50_MINUTES, REVIEW_WAIT_P90_MINUTES),
                MergeWaitTimeStatistics.of(AVG_MERGE_WAIT_MINUTES, MERGED_WITH_APPROVAL_COUNT),
                ReviewCompletionStatistics.of(
                        CORE_TIME_REVIEW_RATE,
                        CORE_TIME_REVIEW_COUNT,
                        SAME_DAY_REVIEW_RATE,
                        SAME_DAY_REVIEW_COUNT
                )
        );

        given(reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(
                eq(USER_ID),
                eq(PROJECT_ID),
                any(ReviewSpeedStatisticsRequest.class)
        ))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                                .queryParam("startDate", START_DATE)
                                .queryParam("endDate", END_DATE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(TOTAL_PR_COUNT))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(REVIEWED_PR_COUNT))
                .andExpect(jsonPath("$.reviewRate").value(REVIEW_RATE))
                .andExpect(jsonPath("$.reviewWaitTime.avgReviewWaitMinutes").value(AVG_REVIEW_WAIT_MINUTES))
                .andExpect(jsonPath("$.reviewWaitTime.reviewWaitP50Minutes").value(REVIEW_WAIT_P50_MINUTES))
                .andExpect(jsonPath("$.reviewWaitTime.reviewWaitP90Minutes").value(REVIEW_WAIT_P90_MINUTES))
                .andExpect(jsonPath("$.mergeWaitTime.avgMergeWaitMinutes").value(AVG_MERGE_WAIT_MINUTES))
                .andExpect(jsonPath("$.mergeWaitTime.mergedWithApprovalCount").value(MERGED_WITH_APPROVAL_COUNT))
                .andExpect(jsonPath("$.reviewCompletion.coreTimeReviewRate").value(CORE_TIME_REVIEW_RATE))
                .andExpect(jsonPath("$.reviewCompletion.coreTimeReviewCount").value(CORE_TIME_REVIEW_COUNT))
                .andExpect(jsonPath("$.reviewCompletion.sameDayReviewRate").value(SAME_DAY_REVIEW_RATE))
                .andExpect(jsonPath("$.reviewCompletion.sameDayReviewCount").value(SAME_DAY_REVIEW_COUNT));

        리뷰_속도_통계_조회_문서화(resultActions);
    }

    private void 리뷰_속도_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("totalPullRequestCount").description("전체 PR 수"),
                                fieldWithPath("reviewedPullRequestCount").description("리뷰가 진행된 PR 수"),
                                fieldWithPath("reviewRate").description("리뷰 진행률 (%)"),
                                fieldWithPath("reviewWaitTime").description("리뷰 대기 시간 통계"),
                                fieldWithPath("reviewWaitTime.avgReviewWaitMinutes").description("평균 리뷰 대기 시간 (분)"),
                                fieldWithPath("reviewWaitTime.reviewWaitP50Minutes").description("리뷰 대기 시간 P50 (분)"),
                                fieldWithPath("reviewWaitTime.reviewWaitP90Minutes").description("리뷰 대기 시간 P90 (분)"),
                                fieldWithPath("mergeWaitTime").description("병합 대기 시간 통계"),
                                fieldWithPath("mergeWaitTime.avgMergeWaitMinutes").description("평균 병합 대기 시간 (분)"),
                                fieldWithPath("mergeWaitTime.mergedWithApprovalCount").description("승인 후 병합된 PR 수"),
                                fieldWithPath("reviewCompletion").description("리뷰 완료 통계"),
                                fieldWithPath("reviewCompletion.coreTimeReviewRate").description("코어타임 내 리뷰 완료율 (%)"),
                                fieldWithPath("reviewCompletion.coreTimeReviewCount").description("코어타임 내 리뷰 완료 PR 수"),
                                fieldWithPath("reviewCompletion.sameDayReviewRate").description("당일 리뷰 완료율 (%)"),
                                fieldWithPath("reviewCompletion.sameDayReviewCount").description("당일 리뷰 완료 PR 수")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = USER_ID)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(
                eq(USER_ID),
                eq(PROJECT_ID),
                any(ReviewSpeedStatisticsRequest.class)
        ))
                .willReturn(ReviewSpeedStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(ZERO_COUNT))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(ZERO_COUNT))
                .andExpect(jsonPath("$.reviewRate").value(ZERO_RATE));
    }

    @Test
    void 인증_정보가_없으면_리뷰_속도_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", PROJECT_ID)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value(MESSAGE_UNAUTHORIZED));
    }

    @Test
    @WithOAuth2User(userId = USER_ID)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(
                eq(USER_ID),
                eq(OTHER_PROJECT_ID),
                any(ReviewSpeedStatisticsRequest.class)
        ))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", OTHER_PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_PROJECT_NOT_FOUND));
    }

    @Test
    @WithOAuth2User(userId = USER_ID)
    void 시작일이_종료일보다_늦으면_400을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                                .queryParam("startDate", INVALID_START_DATE)
                                .queryParam("endDate", INVALID_END_DATE)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_INVALID_DATE_RANGE))
                .andExpect(jsonPath("$.message").value(MESSAGE_INVALID_DATE_RANGE));
    }
}
