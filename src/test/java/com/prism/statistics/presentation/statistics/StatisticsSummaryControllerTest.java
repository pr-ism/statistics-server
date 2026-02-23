package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.StatisticsSummaryQueryService;
import com.prism.statistics.application.statistics.dto.request.StatisticsSummaryRequest;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse.BottleneckSummary;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse.OverviewSummary;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse.ReviewHealthSummary;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse.TeamActivitySummary;
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
class StatisticsSummaryControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private StatisticsSummaryQueryService statisticsSummaryQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 통계_요약_조회_성공_테스트() throws Exception {
        // given
        StatisticsSummaryResponse response = new StatisticsSummaryResponse(
                OverviewSummary.of(100L, 85L, 15L, 85.0, 1440.5, 150.5, "M"),
                ReviewHealthSummary.of(92.0, 120.5, 65.0, 25.0, 8.0),
                TeamActivitySummary.of(12L, 2.5, 1.8, 3.2, 0.35),
                BottleneckSummary.of(120.5, 180.3, 30.2)
        );

        given(statisticsSummaryQueryService.findStatisticsSummary(eq(7L), eq(1L), any(StatisticsSummaryRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/summary", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalPrCount").value(100))
                .andExpect(jsonPath("$.overview.mergedPrCount").value(85))
                .andExpect(jsonPath("$.overview.closedPrCount").value(15))
                .andExpect(jsonPath("$.overview.mergeSuccessRate").value(85.0))
                .andExpect(jsonPath("$.overview.avgMergeTimeMinutes").value(1440.5))
                .andExpect(jsonPath("$.overview.avgSizeScore").value(150.5))
                .andExpect(jsonPath("$.overview.dominantSizeGrade").value("M"))
                .andExpect(jsonPath("$.reviewHealth.reviewRate").value(92.0))
                .andExpect(jsonPath("$.reviewHealth.avgReviewWaitMinutes").value(120.5))
                .andExpect(jsonPath("$.reviewHealth.firstReviewApproveRate").value(65.0))
                .andExpect(jsonPath("$.reviewHealth.changesRequestedRate").value(25.0))
                .andExpect(jsonPath("$.reviewHealth.closedWithoutReviewRate").value(8.0))
                .andExpect(jsonPath("$.teamActivity.totalReviewerCount").value(12))
                .andExpect(jsonPath("$.teamActivity.avgReviewersPerPr").value(2.5))
                .andExpect(jsonPath("$.teamActivity.avgReviewRoundTrips").value(1.8))
                .andExpect(jsonPath("$.teamActivity.avgCommentCount").value(3.2))
                .andExpect(jsonPath("$.teamActivity.reviewerGiniCoefficient").value(0.35))
                .andExpect(jsonPath("$.bottleneck.avgReviewWaitMinutes").value(120.5))
                .andExpect(jsonPath("$.bottleneck.avgReviewProgressMinutes").value(180.3))
                .andExpect(jsonPath("$.bottleneck.avgMergeWaitMinutes").value(30.2))
                .andExpect(jsonPath("$.bottleneck.totalCycleTimeMinutes").value(331.0));

        통계_요약_조회_문서화(resultActions);
    }

    private void 통계_요약_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("overview.totalPrCount").description("전체 PR 수"),
                                fieldWithPath("overview.mergedPrCount").description("Merge된 PR 수"),
                                fieldWithPath("overview.closedPrCount").description("Close된 PR 수 (Merge 제외)"),
                                fieldWithPath("overview.mergeSuccessRate").description("Merge 성공률 (%)"),
                                fieldWithPath("overview.avgMergeTimeMinutes").description("평균 Merge 소요 시간 (분)"),
                                fieldWithPath("overview.avgSizeScore").description("평균 PR 크기 점수"),
                                fieldWithPath("overview.dominantSizeGrade").description("가장 많은 PR 크기 등급"),

                                fieldWithPath("reviewHealth.reviewRate").description("리뷰율 (%)"),
                                fieldWithPath("reviewHealth.avgReviewWaitMinutes").description("평균 리뷰 대기 시간 (분)"),
                                fieldWithPath("reviewHealth.firstReviewApproveRate").description("첫 리뷰 승인율 (%)"),
                                fieldWithPath("reviewHealth.changesRequestedRate").description("수정 요청률 (%)"),
                                fieldWithPath("reviewHealth.closedWithoutReviewRate").description("리뷰 없이 종료된 PR 비율 (%)"),

                                fieldWithPath("teamActivity.totalReviewerCount").description("전체 리뷰어 수"),
                                fieldWithPath("teamActivity.avgReviewersPerPr").description("PR당 평균 리뷰어 수"),
                                fieldWithPath("teamActivity.avgReviewRoundTrips").description("평균 리뷰 왕복 횟수"),
                                fieldWithPath("teamActivity.avgCommentCount").description("평균 코멘트 수"),
                                fieldWithPath("teamActivity.reviewerGiniCoefficient").description("리뷰어 집중도 (지니 계수, 0~1)"),

                                fieldWithPath("bottleneck.avgReviewWaitMinutes").description("평균 리뷰 대기 시간 (분)"),
                                fieldWithPath("bottleneck.avgReviewProgressMinutes").description("평균 리뷰 진행 시간 (분)"),
                                fieldWithPath("bottleneck.avgMergeWaitMinutes").description("평균 Merge 대기 시간 (분)"),
                                fieldWithPath("bottleneck.totalCycleTimeMinutes").description("전체 사이클 타임 (분)")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(statisticsSummaryQueryService.findStatisticsSummary(eq(7L), eq(1L), any(StatisticsSummaryRequest.class)))
                .willReturn(StatisticsSummaryResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/summary", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overview.totalPrCount").value(0))
                .andExpect(jsonPath("$.overview.mergedPrCount").value(0))
                .andExpect(jsonPath("$.overview.mergeSuccessRate").value(0.0))
                .andExpect(jsonPath("$.reviewHealth.reviewRate").value(0.0))
                .andExpect(jsonPath("$.teamActivity.totalReviewerCount").value(0))
                .andExpect(jsonPath("$.bottleneck.avgReviewWaitMinutes").value(0.0));
    }

    @Test
    void 인증_정보가_없으면_통계_요약을_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/summary", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(statisticsSummaryQueryService.findStatisticsSummary(eq(7L), eq(999L), any(StatisticsSummaryRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/summary", 999L)
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
                        get("/projects/{projectId}/statistics/summary", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
