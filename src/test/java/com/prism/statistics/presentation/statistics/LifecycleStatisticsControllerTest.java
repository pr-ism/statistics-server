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

import com.prism.statistics.application.statistics.LifecycleStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.LifecycleStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.LifecycleStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.LifecycleStatisticsResponse.AverageTimeStatistics;
import com.prism.statistics.application.statistics.dto.response.LifecycleStatisticsResponse.HealthStatistics;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class LifecycleStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private LifecycleStatisticsQueryService lifecycleStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void PR_수명주기_통계_조회_성공_테스트() throws Exception {
        // given
        LifecycleStatisticsResponse response = new LifecycleStatisticsResponse(
                100L,
                85L,
                15L,
                85.0,
                AverageTimeStatistics.of(120L, 180L, 60L),
                HealthStatistics.of(5L, 5.0, 3L, 3.0, 2.5)
        );

        given(lifecycleStatisticsQueryService.findLifecycleStatistics(eq(7L), eq(1L), any(LifecycleStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/lifecycle", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(100))
                .andExpect(jsonPath("$.mergedCount").value(85))
                .andExpect(jsonPath("$.closedWithoutMergeCount").value(15))
                .andExpect(jsonPath("$.mergeRate").value(85.0))
                .andExpect(jsonPath("$.averageTime.averageTimeToMergeMinutes").value(120))
                .andExpect(jsonPath("$.averageTime.averageLifespanMinutes").value(180))
                .andExpect(jsonPath("$.averageTime.averageActiveWorkMinutes").value(60))
                .andExpect(jsonPath("$.health.closedWithoutReviewCount").value(5))
                .andExpect(jsonPath("$.health.closedWithoutReviewRate").value(5.0))
                .andExpect(jsonPath("$.health.reopenedCount").value(3))
                .andExpect(jsonPath("$.health.reopenedRate").value(3.0))
                .andExpect(jsonPath("$.health.averageStateChangeCount").value(2.5));

        PR_수명주기_통계_조회_문서화(resultActions);
    }

    private void PR_수명주기_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("mergedCount").description("머지된 PR 수"),
                                fieldWithPath("closedWithoutMergeCount").description("머지 없이 종료된 PR 수"),
                                fieldWithPath("mergeRate").description("머지율 (%)"),
                                fieldWithPath("averageTime").description("평균 시간 통계"),
                                fieldWithPath("averageTime.averageTimeToMergeMinutes").description("평균 머지 소요 시간 (분)"),
                                fieldWithPath("averageTime.averageLifespanMinutes").description("평균 PR 수명 (분)"),
                                fieldWithPath("averageTime.averageActiveWorkMinutes").description("평균 활성 작업 시간 (분)"),
                                fieldWithPath("health").description("PR 건강 지표"),
                                fieldWithPath("health.closedWithoutReviewCount").description("리뷰 없이 종료된 PR 수"),
                                fieldWithPath("health.closedWithoutReviewRate").description("리뷰 없이 종료된 PR 비율 (%)"),
                                fieldWithPath("health.reopenedCount").description("재오픈된 PR 수"),
                                fieldWithPath("health.reopenedRate").description("재오픈된 PR 비율 (%)"),
                                fieldWithPath("health.averageStateChangeCount").description("평균 상태 변경 횟수")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(lifecycleStatisticsQueryService.findLifecycleStatistics(eq(7L), eq(1L), any(LifecycleStatisticsRequest.class)))
                .willReturn(LifecycleStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/lifecycle", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(0))
                .andExpect(jsonPath("$.mergedCount").value(0))
                .andExpect(jsonPath("$.mergeRate").value(0.0));
    }

    @Test
    void 인증_정보가_없으면_PR_수명주기_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/lifecycle", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(lifecycleStatisticsQueryService.findLifecycleStatistics(eq(7L), eq(999L), any(LifecycleStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/lifecycle", 999L)
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
                        get("/projects/{projectId}/statistics/lifecycle", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
