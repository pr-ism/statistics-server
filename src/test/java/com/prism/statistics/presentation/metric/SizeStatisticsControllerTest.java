package com.prism.statistics.presentation.metric;

import static com.prism.statistics.docs.RestDocsConfiguration.field;
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

import com.prism.statistics.application.metric.SizeStatisticsQueryService;
import com.prism.statistics.application.metric.dto.request.SizeStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.SizeStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.SizeStatisticsResponse.SizeStatistics;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.project.exception.ProjectNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class SizeStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private SizeStatisticsQueryService sizeStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void PR_크기_분포_통계_조회_성공_테스트() throws Exception {
        // given
        SizeStatisticsResponse response = new SizeStatisticsResponse(
                List.of(
                        new SizeStatistics("SMALL", 35, 35.0, 2.5, 1.2),
                        new SizeStatistics("MEDIUM", 40, 40.0, 5.3, 3.1),
                        new SizeStatistics("LARGE", 20, 20.0, 12.0, 7.5),
                        new SizeStatistics("EXTRA_LARGE", 5, 5.0, 25.0, 15.0)
                )
        );

        given(sizeStatisticsQueryService.findSizeStatistics(eq(7L), eq(1L), any(SizeStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/size", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sizeStatistics").isArray())
                .andExpect(jsonPath("$.sizeStatistics.length()").value(4))
                .andExpect(jsonPath("$.sizeStatistics[0].sizeCategory").value("SMALL"))
                .andExpect(jsonPath("$.sizeStatistics[0].count").value(35))
                .andExpect(jsonPath("$.sizeStatistics[0].percentage").value(35.0))
                .andExpect(jsonPath("$.sizeStatistics[0].averageChangedFileCount").value(2.5))
                .andExpect(jsonPath("$.sizeStatistics[0].averageCommitCount").value(1.2))
                .andExpect(jsonPath("$.sizeStatistics[3].sizeCategory").value("EXTRA_LARGE"))
                .andExpect(jsonPath("$.sizeStatistics[3].count").value(5))
                .andExpect(jsonPath("$.sizeStatistics[3].percentage").value(5.0));

        PR_크기_분포_통계_조회_문서화(resultActions);
    }

    private void PR_크기_분포_통계_조회_문서화(ResultActions resultActions) throws Exception {
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
                                fieldWithPath("sizeStatistics").description("PR 크기 분포 통계 목록"),
                                fieldWithPath("sizeStatistics[].sizeCategory").description("크기 구간 (SMALL, MEDIUM, LARGE, EXTRA_LARGE)"),
                                fieldWithPath("sizeStatistics[].count").description("해당 구간의 PR 수"),
                                fieldWithPath("sizeStatistics[].percentage").description("해당 구간의 비율 (%)"),
                                fieldWithPath("sizeStatistics[].averageChangedFileCount").description("평균 변경 파일 수"),
                                fieldWithPath("sizeStatistics[].averageCommitCount").description("평균 커밋 수")
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_PR_크기_분포를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/size", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 존재하지_않는_프로젝트의_PR_크기_분포를_조회하면_404를_반환한다() throws Exception {
        // given
        given(sizeStatisticsQueryService.findSizeStatistics(eq(7L), eq(999L), any(SizeStatisticsRequest.class)))
                .willThrow(new ProjectNotFoundException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/size", 999L)
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
                        get("/projects/{projectId}/statistics/size", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
