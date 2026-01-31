package com.prism.statistics.presentation.metric;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.prism.statistics.application.metric.LabelStatisticsQueryService;
import com.prism.statistics.application.metric.dto.request.LabelStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.LabelStatisticsResponse.LabelStatistics;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class LabelStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private LabelStatisticsQueryService labelStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void 라벨별_통계_조회_성공_테스트() throws Exception {
        // given
        LabelStatisticsResponse response = new LabelStatisticsResponse(
                List.of(
                        new LabelStatistics("bug", 5, 500, 200, 100.0, 40.0, 2.0, 3.0),
                        new LabelStatistics("feature", 3, 1200, 300, 400.0, 100.0, 5.0, 8.0),
                        new LabelStatistics("refactor", 2, 400, 360, 200.0, 180.0, 3.0, 6.0)
                )
        );

        given(labelStatisticsQueryService.findLabelStatistics(eq(7L), eq(1L), any(LabelStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/labels", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-31")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labelStatistics").isArray())
                .andExpect(jsonPath("$.labelStatistics.length()").value(3))
                .andExpect(jsonPath("$.labelStatistics[0].labelName").value("bug"))
                .andExpect(jsonPath("$.labelStatistics[0].pullRequestCount").value(5))
                .andExpect(jsonPath("$.labelStatistics[0].totalAdditions").value(500))
                .andExpect(jsonPath("$.labelStatistics[0].totalDeletions").value(200))
                .andExpect(jsonPath("$.labelStatistics[0].averageAdditions").value(100.0))
                .andExpect(jsonPath("$.labelStatistics[0].averageDeletions").value(40.0))
                .andExpect(jsonPath("$.labelStatistics[0].averageCommitCount").value(2.0))
                .andExpect(jsonPath("$.labelStatistics[0].averageChangedFileCount").value(3.0));

        라벨별_통계_조회_문서화(resultActions);
    }

    private void 라벨별_통계_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        queryParameters(
                                parameterWithName("startDate").description("조회 시작 날짜 (YYYY-MM-DD)").optional(),
                                parameterWithName("endDate").description("조회 종료 날짜 (YYYY-MM-DD)").optional()
                        ),
                        responseFields(
                                fieldWithPath("labelStatistics").description("라벨별 통계 목록"),
                                fieldWithPath("labelStatistics[].labelName").description("라벨 이름"),
                                fieldWithPath("labelStatistics[].pullRequestCount").description("PR 수"),
                                fieldWithPath("labelStatistics[].totalAdditions").description("총 추가 라인 수"),
                                fieldWithPath("labelStatistics[].totalDeletions").description("총 삭제 라인 수"),
                                fieldWithPath("labelStatistics[].averageAdditions").description("평균 추가 라인 수"),
                                fieldWithPath("labelStatistics[].averageDeletions").description("평균 삭제 라인 수"),
                                fieldWithPath("labelStatistics[].averageCommitCount").description("평균 커밋 수"),
                                fieldWithPath("labelStatistics[].averageChangedFileCount").description("평균 변경 파일 수")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_라벨별_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/labels", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 존재하지_않는_프로젝트의_라벨별_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(labelStatisticsQueryService.findLabelStatistics(eq(7L), eq(999L), any(LabelStatisticsRequest.class)))
                .willThrow(new ProjectNotFoundException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/labels", 999L)
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
                       get("/projects/{projectId}/statistics/labels", 1L)
                               .header("Authorization", "Bearer access-token")
                               .queryParam("startDate", "2024-02-01")
                               .queryParam("endDate", "2024-01-01")
               )
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.errorCode").value("D03"))
               .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
