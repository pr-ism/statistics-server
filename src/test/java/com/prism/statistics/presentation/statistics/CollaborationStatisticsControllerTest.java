package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.CollaborationStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.CollaborationStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse.*;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

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
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
class CollaborationStatisticsControllerTest extends CommonControllerSliceTestSupport {

    private static final long USER_ID = 7L;
    private static final long PROJECT_ID = 1L;
    private static final long OTHER_PROJECT_ID = 999L;
    private static final long TOTAL_PULL_REQUEST_COUNT = 100L;
    private static final long REVIEWED_PULL_REQUEST_COUNT = 85L;
    private static final long TOTAL_REVIEWER_COUNT = 8L;
    private static final long REPEATED_DRAFT_COUNT = 5L;
    private static final long REVIEWER_ADDED_COUNT = 15L;
    private static final long REVIEW_COUNT_REVIEWER_1 = 25L;
    private static final long REVIEW_COUNT_REVIEWER_2 = 20L;
    private static final long AUTHOR_ID_1 = 1L;
    private static final long AUTHOR_ID_2 = 2L;
    private static final long REVIEWER_ID_1 = 1L;
    private static final long REVIEWER_ID_2 = 2L;
    private static final long AUTHOR_PR_COUNT_1 = 10L;
    private static final long AUTHOR_PR_COUNT_2 = 8L;
    private static final double GINI_COEFFICIENT = 0.35;
    private static final double TOP3_REVIEWER_RATE = 65.0;
    private static final double REPEATED_DRAFT_RATE = 5.0;
    private static final double REVIEWER_ADDED_RATE = 15.0;
    private static final double AVG_REVIEW_WAIT_MINUTES_1 = 120.5;
    private static final double AVG_REVIEW_WAIT_MINUTES_2 = 90.0;
    private static final double AVG_RESPONSE_TIME_MINUTES_1 = 45.5;
    private static final double AVG_RESPONSE_TIME_MINUTES_2 = 60.0;
    private static final String AUTHOR_NAME_1 = "author1";
    private static final String AUTHOR_NAME_2 = "author2";
    private static final String REVIEWER_NAME_1 = "reviewer1";
    private static final String REVIEWER_NAME_2 = "reviewer2";
    private static final String ACCESS_TOKEN = "Bearer access-token";
    private static final String START_DATE = "2026-01-01";
    private static final String END_DATE = "2026-02-01";
    private static final String INVALID_START_DATE = "2024-02-01";
    private static final String INVALID_END_DATE = "2024-01-01";
    private static final String ERROR_CODE_FORBIDDEN = "A04";
    private static final String ERROR_CODE_PROJECT_NOT_FOUND = "P00";
    private static final String ERROR_CODE_INVALID_DATE = "D03";
    private static final String MESSAGE_UNAUTHORIZED = "인가되지 않은 회원";
    private static final String MESSAGE_INVALID_DATE_RANGE = "종료일은 시작일보다 빠를 수 없습니다.";
    private static final long ZERO_LONG = 0L;

    @Autowired
    private CollaborationStatisticsQueryService collaborationStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 협업_통계_조회_성공_테스트() throws Exception {
        // given
        CollaborationStatisticsResponse response = new CollaborationStatisticsResponse(
                TOTAL_PULL_REQUEST_COUNT,
                REVIEWED_PULL_REQUEST_COUNT,
                ReviewerConcentrationStatistics.of(GINI_COEFFICIENT, TOP3_REVIEWER_RATE, TOTAL_REVIEWER_COUNT),
                DraftPrStatistics.of(REPEATED_DRAFT_RATE, REPEATED_DRAFT_COUNT),
                ReviewerAdditionStatistics.of(REVIEWER_ADDED_RATE, REVIEWER_ADDED_COUNT),
                List.of(
                        AuthorReviewWaitTime.of(AUTHOR_ID_1, AUTHOR_NAME_1, AVG_REVIEW_WAIT_MINUTES_1, AUTHOR_PR_COUNT_1),
                        AuthorReviewWaitTime.of(AUTHOR_ID_2, AUTHOR_NAME_2, AVG_REVIEW_WAIT_MINUTES_2, AUTHOR_PR_COUNT_2)
                ),
                List.of(
                        ReviewerStats.of(REVIEWER_ID_1, REVIEWER_NAME_1, REVIEW_COUNT_REVIEWER_1, AVG_RESPONSE_TIME_MINUTES_1),
                        ReviewerStats.of(REVIEWER_ID_2, REVIEWER_NAME_2, REVIEW_COUNT_REVIEWER_2, AVG_RESPONSE_TIME_MINUTES_2)
                )
        );

        given(collaborationStatisticsQueryService.findCollaborationStatistics(eq(USER_ID), eq(PROJECT_ID), any(CollaborationStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", PROJECT_ID)
                                .header("Authorization", ACCESS_TOKEN)
                                .queryParam("startDate", START_DATE)
                                .queryParam("endDate", END_DATE)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(TOTAL_PULL_REQUEST_COUNT))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(REVIEWED_PULL_REQUEST_COUNT))
                .andExpect(jsonPath("$.reviewerConcentration.giniCoefficient").value(GINI_COEFFICIENT))
                .andExpect(jsonPath("$.reviewerConcentration.top3ReviewerRate").value(TOP3_REVIEWER_RATE))
                .andExpect(jsonPath("$.reviewerConcentration.totalReviewerCount").value(TOTAL_REVIEWER_COUNT))
                .andExpect(jsonPath("$.draftPr.repeatedDraftPrRate").value(REPEATED_DRAFT_RATE))
                .andExpect(jsonPath("$.draftPr.repeatedDraftPrCount").value(REPEATED_DRAFT_COUNT))
                .andExpect(jsonPath("$.reviewerAddition.reviewerAddedRate").value(REVIEWER_ADDED_RATE))
                .andExpect(jsonPath("$.reviewerAddition.reviewerAddedPrCount").value(REVIEWER_ADDED_COUNT))
                .andExpect(jsonPath("$.authorReviewWaitTimes[0].authorName").value(AUTHOR_NAME_1))
                .andExpect(jsonPath("$.authorReviewWaitTimes[0].avgReviewWaitMinutes").value(AVG_REVIEW_WAIT_MINUTES_1))
                .andExpect(jsonPath("$.reviewerStats[0].reviewerName").value(REVIEWER_NAME_1))
                .andExpect(jsonPath("$.reviewerStats[0].reviewCount").value(REVIEW_COUNT_REVIEWER_1));

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
        given(collaborationStatisticsQueryService.findCollaborationStatistics(eq(USER_ID), eq(PROJECT_ID), any(CollaborationStatisticsRequest.class)))
                .willReturn(CollaborationStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", PROJECT_ID)
                                .header("Authorization", ACCESS_TOKEN)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPullRequestCount").value(ZERO_LONG))
                .andExpect(jsonPath("$.reviewedPullRequestCount").value(ZERO_LONG));
    }

    @Test
    void 인증_정보가_없으면_협업_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", PROJECT_ID)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_FORBIDDEN))
                .andExpect(jsonPath("$.message").value(MESSAGE_UNAUTHORIZED));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(collaborationStatisticsQueryService.findCollaborationStatistics(eq(USER_ID), eq(OTHER_PROJECT_ID), any(CollaborationStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", OTHER_PROJECT_ID)
                                .header("Authorization", ACCESS_TOKEN)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_PROJECT_NOT_FOUND));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 시작일이_종료일보다_늦으면_400을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/collaboration", PROJECT_ID)
                                .header("Authorization", ACCESS_TOKEN)
                                .queryParam("startDate", INVALID_START_DATE)
                                .queryParam("endDate", INVALID_END_DATE)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ERROR_CODE_INVALID_DATE))
                .andExpect(jsonPath("$.message").value(MESSAGE_INVALID_DATE_RANGE));
    }
}
