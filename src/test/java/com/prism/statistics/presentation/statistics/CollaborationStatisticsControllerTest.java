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

import com.prism.statistics.application.statistics.CollaborationStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.CollaborationStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.AuthorReviewWaitTime;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.DraftPrStatistics;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.ReviewerAdditionStatistics;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.ReviewerConcentrationStatistics;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.ReviewerStats;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class CollaborationStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private CollaborationStatisticsQueryService collaborationStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 협업_통계_조회_성공_테스트() throws Exception {
        // given
        CollaborationStatisticsResponse response = new CollaborationStatisticsResponse(
                100L,
                85L,
                ReviewerConcentrationStatistics.of(0.35, 65.0, 8L),
                DraftPrStatistics.of(5.0, 5L),
                ReviewerAdditionStatistics.of(15.0, 15L),
                List.of(
                        AuthorReviewWaitTime.of(1L, "author1", 120.5, 10L),
                        AuthorReviewWaitTime.of(2L, "author2", 90.0, 8L)
                ),
                List.of(
                        ReviewerStats.of(1L, "reviewer1", 25L, 45.5),
                        ReviewerStats.of(2L, "reviewer2", 20L, 60.0)
                )
        );

        given(collaborationStatisticsQueryService.findCollaborationStatistics(eq(7L), eq(1L), any(CollaborationStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(100))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(85))
                .andExpect(jsonPath("$.reviewerConcentration.giniCoefficient").value(0.35))
                .andExpect(jsonPath("$.reviewerConcentration.top3ReviewerRate").value(65.0))
                .andExpect(jsonPath("$.reviewerConcentration.totalReviewerCount").value(8))
                .andExpect(jsonPath("$.draftPr.repeatedDraftPrRate").value(5.0))
                .andExpect(jsonPath("$.draftPr.repeatedDraftPrCount").value(5))
                .andExpect(jsonPath("$.reviewerAddition.reviewerAddedRate").value(15.0))
                .andExpect(jsonPath("$.reviewerAddition.reviewerAddedPrCount").value(15))
                .andExpect(jsonPath("$.authorReviewWaitTimes[0].authorName").value("author1"))
                .andExpect(jsonPath("$.authorReviewWaitTimes[0].avgReviewWaitMinutes").value(120.5))
                .andExpect(jsonPath("$.reviewerStats[0].reviewerName").value("reviewer1"))
                .andExpect(jsonPath("$.reviewerStats[0].reviewCount").value(25));

        협업_통계_조회_문서화(resultActions);
    }

    private void 협업_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("reviewerConcentration").description("리뷰어 집중도 통계"),
                                fieldWithPath("reviewerConcentration.giniCoefficient").description("리뷰어 선정 집중도 (Gini 계수, 0~1)"),
                                fieldWithPath("reviewerConcentration.top3ReviewerRate").description("상위 3명 리뷰어 리뷰 비율 (%)"),
                                fieldWithPath("reviewerConcentration.totalReviewerCount").description("전체 리뷰어 수"),
                                fieldWithPath("draftPr").description("Draft PR 통계"),
                                fieldWithPath("draftPr.repeatedDraftPrRate").description("반복 Draft PR 비율 (%)"),
                                fieldWithPath("draftPr.repeatedDraftPrCount").description("반복 Draft PR 수"),
                                fieldWithPath("reviewerAddition").description("리뷰어 추가 통계"),
                                fieldWithPath("reviewerAddition.reviewerAddedRate").description("리뷰어 추가 발생률 (%)"),
                                fieldWithPath("reviewerAddition.reviewerAddedPrCount").description("리뷰어 추가된 PR 수"),
                                fieldWithPath("authorReviewWaitTimes").description("작성자별 리뷰 대기 시간 목록"),
                                fieldWithPath("authorReviewWaitTimes[].authorId").description("작성자 ID"),
                                fieldWithPath("authorReviewWaitTimes[].authorName").description("작성자 이름"),
                                fieldWithPath("authorReviewWaitTimes[].avgReviewWaitMinutes").description("평균 리뷰 대기 시간 (분)"),
                                fieldWithPath("authorReviewWaitTimes[].prCount").description("PR 수"),
                                fieldWithPath("reviewerStats").description("리뷰어별 통계 목록"),
                                fieldWithPath("reviewerStats[].reviewerId").description("리뷰어 ID"),
                                fieldWithPath("reviewerStats[].reviewerName").description("리뷰어 이름"),
                                fieldWithPath("reviewerStats[].reviewCount").description("리뷰 수"),
                                fieldWithPath("reviewerStats[].avgResponseTimeMinutes").description("평균 응답 시간 (분)")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(collaborationStatisticsQueryService.findCollaborationStatistics(eq(7L), eq(1L), any(CollaborationStatisticsRequest.class)))
                .willReturn(CollaborationStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(0))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(0));
    }

    @Test
    void 인증_정보가_없으면_협업_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(collaborationStatisticsQueryService.findCollaborationStatistics(eq(7L), eq(999L), any(CollaborationStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", 999L)
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
                        get("/projects/{projectId}/statistics/collaboration", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
