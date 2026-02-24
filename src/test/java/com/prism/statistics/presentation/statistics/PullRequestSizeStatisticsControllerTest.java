package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.PullRequestSizeStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.PullRequestSizeStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.PullRequestSizeStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.PullRequestSizeStatisticsResponse.CorrelationStatistics;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

import java.util.EnumMap;
import java.util.Map;

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
class PullRequestSizeStatisticsControllerTest extends CommonControllerSliceTestSupport {

    private static final long USER_ID = 7L;
    private static final long PROJECT_ID = 1L;
    private static final long OTHER_PROJECT_ID = 999L;
    private static final long TOTAL_PR_COUNT = 100L;
    private static final double AVG_SIZE_SCORE = 185.5;
    private static final double LARGE_PR_RATE = 30.0;
    private static final double REVIEW_WAIT_CORRELATION = 0.65;
    private static final double REVIEW_ROUND_TRIP_CORRELATION = 0.42;
    private static final long XS_COUNT = 10L;
    private static final long S_COUNT = 25L;
    private static final long M_COUNT = 35L;
    private static final long L_COUNT = 20L;
    private static final long XL_COUNT = 10L;
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
    private static final String INTERPRETATION_STRONG_POSITIVE = "다소 강한 양의 상관관계";
    private static final String INTERPRETATION_MODERATE_POSITIVE = "보통 양의 상관관계";

    @Autowired
    private PullRequestSizeStatisticsQueryService pullRequestSizeStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = USER_ID)
    void PR_사이즈_통계_조회_성공_테스트() throws Exception {
        // given
        Map<SizeGrade, Long> sizeGradeDistribution = new EnumMap<>(SizeGrade.class);
        sizeGradeDistribution.put(SizeGrade.XS, XS_COUNT);
        sizeGradeDistribution.put(SizeGrade.S, S_COUNT);
        sizeGradeDistribution.put(SizeGrade.M, M_COUNT);
        sizeGradeDistribution.put(SizeGrade.L, L_COUNT);
        sizeGradeDistribution.put(SizeGrade.XL, XL_COUNT);

        PullRequestSizeStatisticsResponse response = new PullRequestSizeStatisticsResponse(
                TOTAL_PR_COUNT,
                AVG_SIZE_SCORE,
                sizeGradeDistribution,
                LARGE_PR_RATE,
                CorrelationStatistics.of(REVIEW_WAIT_CORRELATION),
                CorrelationStatistics.of(REVIEW_ROUND_TRIP_CORRELATION)
        );

        given(pullRequestSizeStatisticsQueryService.findPullRequestSizeStatistics(
                eq(USER_ID),
                eq(PROJECT_ID),
                any(PullRequestSizeStatisticsRequest.class)
        ))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/pullrequest-size", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                                .queryParam("startDate", START_DATE)
                                .queryParam("endDate", END_DATE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(TOTAL_PR_COUNT))
                .andExpect(jsonPath("$.avgSizeScore").value(AVG_SIZE_SCORE))
                .andExpect(jsonPath("$.sizeGradeDistribution.XS").value(XS_COUNT))
                .andExpect(jsonPath("$.sizeGradeDistribution.S").value(S_COUNT))
                .andExpect(jsonPath("$.sizeGradeDistribution.M").value(M_COUNT))
                .andExpect(jsonPath("$.sizeGradeDistribution.L").value(L_COUNT))
                .andExpect(jsonPath("$.sizeGradeDistribution.XL").value(XL_COUNT))
                .andExpect(jsonPath("$.largePullRequestRate").value(LARGE_PR_RATE))
                .andExpect(jsonPath("$.sizeReviewWaitCorrelation.correlationCoefficient").value(REVIEW_WAIT_CORRELATION))
                .andExpect(jsonPath("$.sizeReviewWaitCorrelation.interpretation").value(INTERPRETATION_STRONG_POSITIVE))
                .andExpect(jsonPath("$.sizeReviewRoundTripCorrelation.correlationCoefficient").value(REVIEW_ROUND_TRIP_CORRELATION))
                .andExpect(jsonPath("$.sizeReviewRoundTripCorrelation.interpretation").value(INTERPRETATION_MODERATE_POSITIVE));

        PR_사이즈_통계_조회_문서화(resultActions);
    }

    private void PR_사이즈_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("avgSizeScore").description("평균 PR 사이즈 점수"),
                                fieldWithPath("sizeGradeDistribution").description("사이즈 등급별 PR 분포"),
                                fieldWithPath("sizeGradeDistribution.XS").description("Extra Small 등급 PR 수 (0-10)"),
                                fieldWithPath("sizeGradeDistribution.S").description("Small 등급 PR 수 (10-100)"),
                                fieldWithPath("sizeGradeDistribution.M").description("Medium 등급 PR 수 (100-300)"),
                                fieldWithPath("sizeGradeDistribution.L").description("Large 등급 PR 수 (300-1000)"),
                                fieldWithPath("sizeGradeDistribution.XL").description("Extra Large 등급 PR 수 (1000+)"),
                                fieldWithPath("largePullRequestRate").description("Large 이상 PR 비율 (%)"),
                                fieldWithPath("sizeReviewWaitCorrelation").description("PR 사이즈-리뷰 대기시간 상관관계"),
                                fieldWithPath("sizeReviewWaitCorrelation.correlationCoefficient").description("Pearson 상관계수 (-1 ~ 1)"),
                                fieldWithPath("sizeReviewWaitCorrelation.interpretation").description("상관관계 해석"),
                                fieldWithPath("sizeReviewRoundTripCorrelation").description("PR 사이즈-리뷰 라운드트립 상관관계"),
                                fieldWithPath("sizeReviewRoundTripCorrelation.correlationCoefficient").description("Pearson 상관계수 (-1 ~ 1)"),
                                fieldWithPath("sizeReviewRoundTripCorrelation.interpretation").description("상관관계 해석")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = USER_ID)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(pullRequestSizeStatisticsQueryService.findPullRequestSizeStatistics(
                eq(USER_ID),
                eq(PROJECT_ID),
                any(PullRequestSizeStatisticsRequest.class)
        ))
                .willReturn(PullRequestSizeStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/pullrequest-size", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(ZERO_COUNT))
                .andExpect(jsonPath("$.avgSizeScore").value(ZERO_RATE))
                .andExpect(jsonPath("$.largePullRequestRate").value(ZERO_RATE));
    }

    @Test
    void 인증_정보가_없으면_PR_사이즈_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/pullrequest-size", PROJECT_ID)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_UNAUTHORIZED))
                .andExpect(jsonPath("$.message").value(MESSAGE_UNAUTHORIZED));
    }

    @Test
    @WithOAuth2User(userId = USER_ID)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(pullRequestSizeStatisticsQueryService.findPullRequestSizeStatistics(
                eq(USER_ID),
                eq(OTHER_PROJECT_ID),
                any(PullRequestSizeStatisticsRequest.class)
        ))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/pullrequest-size", OTHER_PROJECT_ID)
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
                        get("/projects/{projectId}/statistics/pullrequest-size", PROJECT_ID)
                                .header(AUTH_HEADER, BEARER_TOKEN)
                                .queryParam("startDate", INVALID_START_DATE)
                                .queryParam("endDate", INVALID_END_DATE)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_INVALID_DATE_RANGE))
                .andExpect(jsonPath("$.message").value(MESSAGE_INVALID_DATE_RANGE));
    }
}
