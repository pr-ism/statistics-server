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

    private static final long USER_ID = 7L;
    private static final long PROJECT_ID = 1L;
    private static final long OTHER_PROJECT_ID = 999L;
    private static final long TOTAL_PR_COUNT = 100L;
    private static final long REVIEWED_PR_COUNT = 85L;
    private static final double REVIEW_RATE = 85.0;
    private static final double AVG_REVIEW_ROUND_TRIPS = 2.5;
    private static final double AVG_COMMENT_COUNT = 8.3;
    private static final double AVG_COMMENT_DENSITY = 0.05;
    private static final long WITH_ADDITIONAL_REVIEWERS_COUNT = 10L;
    private static final long WITH_CHANGES_AFTER_REVIEW_COUNT = 15L;
    private static final double FIRST_REVIEW_APPROVE_RATE = 45.0;
    private static final double POST_REVIEW_COMMIT_RATE = 17.65;
    private static final double CHANGES_REQUESTED_RATE = 20.0;
    private static final double AVG_CHANGES_RESOLUTION_MINUTES = 120.5;
    private static final double HIGH_INTENSITY_PR_RATE = 5.0;
    private static final long TOTAL_REVIEWER_COUNT = 12L;
    private static final double AVG_REVIEWERS_PER_PR = 1.8;
    private static final double AVG_SESSION_DURATION_MINUTES = 45.5;
    private static final double AVG_REVIEWS_PER_SESSION = 1.2;
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
    private static final int ZERO_COUNT = 0;
    private static final double ZERO_RATE = 0.0;

    @Autowired
    private ReviewQualityStatisticsQueryService reviewQualityStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = USER_ID)
    void 리뷰_품질_통계_조회_성공_테스트() throws Exception {
        // given
        ReviewQualityStatisticsResponse response = new ReviewQualityStatisticsResponse(
                TOTAL_PR_COUNT,
                REVIEWED_PR_COUNT,
                REVIEW_RATE,
                ReviewActivityStatistics.of(
                        AVG_REVIEW_ROUND_TRIPS,
                        AVG_COMMENT_COUNT,
                        AVG_COMMENT_DENSITY,
                        WITH_ADDITIONAL_REVIEWERS_COUNT,
                        WITH_CHANGES_AFTER_REVIEW_COUNT,
                        FIRST_REVIEW_APPROVE_RATE,
                        POST_REVIEW_COMMIT_RATE,
                        CHANGES_REQUESTED_RATE,
                        AVG_CHANGES_RESOLUTION_MINUTES,
                        HIGH_INTENSITY_PR_RATE
                ),
                ReviewerStatistics.of(
                        TOTAL_REVIEWER_COUNT,
                        AVG_REVIEWERS_PER_PR,
                        AVG_SESSION_DURATION_MINUTES,
                        AVG_REVIEWS_PER_SESSION
                )
        );

        given(reviewQualityStatisticsQueryService.findReviewQualityStatistics(
                eq(USER_ID),
                eq(PROJECT_ID),
                any(ReviewQualityStatisticsRequest.class)
        ))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-quality", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                                .queryParam("startDate", START_DATE)
                                .queryParam("endDate", END_DATE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(TOTAL_PR_COUNT))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(REVIEWED_PR_COUNT))
                .andExpect(jsonPath("$.reviewRate").value(REVIEW_RATE))
                .andExpect(jsonPath("$.reviewActivity.avgReviewRoundTrips").value(AVG_REVIEW_ROUND_TRIPS))
                .andExpect(jsonPath("$.reviewActivity.avgCommentCount").value(AVG_COMMENT_COUNT))
                .andExpect(jsonPath("$.reviewActivity.avgCommentDensity").value(AVG_COMMENT_DENSITY))
                .andExpect(jsonPath("$.reviewActivity.withAdditionalReviewersCount").value(WITH_ADDITIONAL_REVIEWERS_COUNT))
                .andExpect(jsonPath("$.reviewActivity.withChangesAfterReviewCount").value(WITH_CHANGES_AFTER_REVIEW_COUNT))
                .andExpect(jsonPath("$.reviewActivity.firstReviewApproveRate").value(FIRST_REVIEW_APPROVE_RATE))
                .andExpect(jsonPath("$.reviewActivity.postReviewCommitRate").value(POST_REVIEW_COMMIT_RATE))
                .andExpect(jsonPath("$.reviewActivity.changesRequestedRate").value(CHANGES_REQUESTED_RATE))
                .andExpect(jsonPath("$.reviewActivity.avgChangesResolutionMinutes").value(AVG_CHANGES_RESOLUTION_MINUTES))
                .andExpect(jsonPath("$.reviewActivity.highIntensityPrRate").value(HIGH_INTENSITY_PR_RATE))
                .andExpect(jsonPath("$.reviewerStats.totalReviewerCount").value(TOTAL_REVIEWER_COUNT))
                .andExpect(jsonPath("$.reviewerStats.avgReviewersPerPr").value(AVG_REVIEWERS_PER_PR))
                .andExpect(jsonPath("$.reviewerStats.avgSessionDurationMinutes").value(AVG_SESSION_DURATION_MINUTES))
                .andExpect(jsonPath("$.reviewerStats.avgReviewsPerSession").value(AVG_REVIEWS_PER_SESSION));

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
    @WithOAuth2User(userId = USER_ID)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(reviewQualityStatisticsQueryService.findReviewQualityStatistics(
                eq(USER_ID),
                eq(PROJECT_ID),
                any(ReviewQualityStatisticsRequest.class)
        ))
                .willReturn(ReviewQualityStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-quality", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(ZERO_COUNT))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(ZERO_COUNT))
                .andExpect(jsonPath("$.reviewRate").value(ZERO_RATE));
    }

    @Test
    void 인증_정보가_없으면_리뷰_품질_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-quality", PROJECT_ID)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value(MESSAGE_UNAUTHORIZED));
    }

    @Test
    @WithOAuth2User(userId = USER_ID)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(reviewQualityStatisticsQueryService.findReviewQualityStatistics(
                eq(USER_ID),
                eq(OTHER_PROJECT_ID),
                any(ReviewQualityStatisticsRequest.class)
        ))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/review-quality", OTHER_PROJECT_ID)
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
                        get("/projects/{projectId}/statistics/review-quality", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                                .queryParam("startDate", INVALID_START_DATE)
                                .queryParam("endDate", INVALID_END_DATE)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_INVALID_DATE_RANGE))
                .andExpect(jsonPath("$.message").value(MESSAGE_INVALID_DATE_RANGE));
    }
}
