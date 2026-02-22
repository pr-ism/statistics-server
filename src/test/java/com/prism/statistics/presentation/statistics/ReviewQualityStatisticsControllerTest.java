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

import com.prism.statistics.application.statistics.ReviewQualityStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.ReviewQualityStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse.ReviewActivityStatistics;
import com.prism.statistics.application.statistics.dto.response.ReviewQualityStatisticsResponse.ReviewerStatistics;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class ReviewQualityStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private ReviewQualityStatisticsQueryService reviewQualityStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 리뷰_품질_통계_조회_성공_테스트() throws Exception {
        // given
        ReviewQualityStatisticsResponse response = new ReviewQualityStatisticsResponse(
                100L,
                85L,
                85.0,
                ReviewActivityStatistics.of(2.5, 8.3, 0.05, 10L, 15L, 45.0, 17.65, 20.0, 120.5, 5.0),
                ReviewerStatistics.of(12L, 1.8, 45.5, 1.2)
        );

        given(reviewQualityStatisticsQueryService.findReviewQualityStatistics(eq(7L), eq(1L), any(ReviewQualityStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-quality", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(100))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(85))
                .andExpect(jsonPath("$.reviewRate").value(85.0))
                .andExpect(jsonPath("$.reviewActivity.avgReviewRoundTrips").value(2.5))
                .andExpect(jsonPath("$.reviewActivity.avgCommentCount").value(8.3))
                .andExpect(jsonPath("$.reviewActivity.avgCommentDensity").value(0.05))
                .andExpect(jsonPath("$.reviewActivity.withAdditionalReviewersCount").value(10))
                .andExpect(jsonPath("$.reviewActivity.withChangesAfterReviewCount").value(15))
                .andExpect(jsonPath("$.reviewActivity.firstReviewApproveRate").value(45.0))
                .andExpect(jsonPath("$.reviewActivity.postReviewCommitRate").value(17.65))
                .andExpect(jsonPath("$.reviewActivity.changesRequestedRate").value(20.0))
                .andExpect(jsonPath("$.reviewActivity.avgChangesResolutionMinutes").value(120.5))
                .andExpect(jsonPath("$.reviewActivity.highIntensityPrRate").value(5.0))
                .andExpect(jsonPath("$.reviewerStats.totalReviewerCount").value(12))
                .andExpect(jsonPath("$.reviewerStats.avgReviewersPerPr").value(1.8))
                .andExpect(jsonPath("$.reviewerStats.avgSessionDurationMinutes").value(45.5))
                .andExpect(jsonPath("$.reviewerStats.avgReviewsPerSession").value(1.2));

        리뷰_품질_통계_조회_문서화(resultActions);
    }

    private void 리뷰_품질_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("reviewActivity").description("리뷰 활동 통계"),
                                fieldWithPath("reviewActivity.avgReviewRoundTrips").description("평균 리뷰 라운드트립 횟수"),
                                fieldWithPath("reviewActivity.avgCommentCount").description("평균 코멘트 수"),
                                fieldWithPath("reviewActivity.avgCommentDensity").description("평균 코멘트 밀도 (코멘트/코드변경량)"),
                                fieldWithPath("reviewActivity.withAdditionalReviewersCount").description("추가 리뷰어가 있는 PR 수"),
                                fieldWithPath("reviewActivity.withChangesAfterReviewCount").description("리뷰 후 코드 변경이 있는 PR 수"),
                                fieldWithPath("reviewActivity.firstReviewApproveRate").description("첫 리뷰 승인 비율 (%)"),
                                fieldWithPath("reviewActivity.postReviewCommitRate").description("리뷰 후 커밋 발생률 (%)"),
                                fieldWithPath("reviewActivity.changesRequestedRate").description("변경 요청 비율 (%)"),
                                fieldWithPath("reviewActivity.avgChangesResolutionMinutes").description("평균 변경 요청 해결 시간 (분)"),
                                fieldWithPath("reviewActivity.highIntensityPrRate").description("고강도 PR 비율 (%)"),
                                fieldWithPath("reviewerStats").description("리뷰어 통계"),
                                fieldWithPath("reviewerStats.totalReviewerCount").description("전체 리뷰어 수"),
                                fieldWithPath("reviewerStats.avgReviewersPerPr").description("PR당 평균 리뷰어 수"),
                                fieldWithPath("reviewerStats.avgSessionDurationMinutes").description("평균 리뷰 세션 시간 (분)"),
                                fieldWithPath("reviewerStats.avgReviewsPerSession").description("세션당 평균 리뷰 횟수")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(reviewQualityStatisticsQueryService.findReviewQualityStatistics(eq(7L), eq(1L), any(ReviewQualityStatisticsRequest.class)))
                .willReturn(ReviewQualityStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-quality", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(0))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(0))
                .andExpect(jsonPath("$.reviewRate").value(0.0));
    }

    @Test
    void 인증_정보가_없으면_리뷰_품질_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-quality", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(reviewQualityStatisticsQueryService.findReviewQualityStatistics(eq(7L), eq(999L), any(ReviewQualityStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-quality", 999L)
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
                        get("/projects/{projectId}/statistics/review-quality", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
