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

@SuppressWarnings("NonAsciiCharacters")
class ReviewSpeedStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private ReviewSpeedStatisticsQueryService reviewSpeedStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 리뷰_속도_통계_조회_성공_테스트() throws Exception {
        // given
        ReviewSpeedStatisticsResponse response = new ReviewSpeedStatisticsResponse(
                100L,
                85L,
                85.0,
                ReviewWaitTimeStatistics.of(120.5, 90.0, 240.0),
                MergeWaitTimeStatistics.of(30.5, 70L),
                ReviewCompletionStatistics.of(65.0, 55L, 45.0, 38L)
        );

        given(reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(eq(7L), eq(1L), any(ReviewSpeedStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(100))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(85))
                .andExpect(jsonPath("$.reviewRate").value(85.0))
                .andExpect(jsonPath("$.reviewWaitTime.avgReviewWaitMinutes").value(120.5))
                .andExpect(jsonPath("$.reviewWaitTime.reviewWaitP50Minutes").value(90.0))
                .andExpect(jsonPath("$.reviewWaitTime.reviewWaitP90Minutes").value(240.0))
                .andExpect(jsonPath("$.mergeWaitTime.avgMergeWaitMinutes").value(30.5))
                .andExpect(jsonPath("$.mergeWaitTime.mergedWithApprovalCount").value(70))
                .andExpect(jsonPath("$.reviewCompletion.coreTimeReviewRate").value(65.0))
                .andExpect(jsonPath("$.reviewCompletion.coreTimeReviewCount").value(55))
                .andExpect(jsonPath("$.reviewCompletion.sameDayReviewRate").value(45.0))
                .andExpect(jsonPath("$.reviewCompletion.sameDayReviewCount").value(38));

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
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(eq(7L), eq(1L), any(ReviewSpeedStatisticsRequest.class)))
                .willReturn(ReviewSpeedStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(0))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(0))
                .andExpect(jsonPath("$.reviewRate").value(0.0));
    }

    @Test
    void 인증_정보가_없으면_리뷰_속도_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(reviewSpeedStatisticsQueryService.findReviewSpeedStatistics(eq(7L), eq(999L), any(ReviewSpeedStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-speed", 999L)
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
                        get("/projects/{projectId}/statistics/review-speed", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
