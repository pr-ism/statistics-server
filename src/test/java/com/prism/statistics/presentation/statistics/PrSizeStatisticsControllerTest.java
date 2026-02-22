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

import com.prism.statistics.application.statistics.PrSizeStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.PrSizeStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.PrSizeStatisticsResponse;
import com.prism.statistics.application.statistics.dto.response.PrSizeStatisticsResponse.CorrelationStatistics;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class PrSizeStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private PrSizeStatisticsQueryService prSizeStatisticsQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void PR_사이즈_통계_조회_성공_테스트() throws Exception {
        // given
        Map<SizeGrade, Long> sizeGradeDistribution = new EnumMap<>(SizeGrade.class);
        sizeGradeDistribution.put(SizeGrade.XS, 10L);
        sizeGradeDistribution.put(SizeGrade.S, 25L);
        sizeGradeDistribution.put(SizeGrade.M, 35L);
        sizeGradeDistribution.put(SizeGrade.L, 20L);
        sizeGradeDistribution.put(SizeGrade.XL, 10L);

        PrSizeStatisticsResponse response = new PrSizeStatisticsResponse(
                100L,
                185.5,
                sizeGradeDistribution,
                30.0,
                CorrelationStatistics.of(0.65),
                CorrelationStatistics.of(0.42)
        );

        given(prSizeStatisticsQueryService.findPrSizeStatistics(eq(7L), eq(1L), any(PrSizeStatisticsRequest.class)))
                .willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/statistics/pr-size", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2026-01-01")
                                .queryParam("endDate", "2026-02-01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrCount").value(100))
                .andExpect(jsonPath("$.avgSizeScore").value(185.5))
                .andExpect(jsonPath("$.sizeGradeDistribution.XS").value(10))
                .andExpect(jsonPath("$.sizeGradeDistribution.S").value(25))
                .andExpect(jsonPath("$.sizeGradeDistribution.M").value(35))
                .andExpect(jsonPath("$.sizeGradeDistribution.L").value(20))
                .andExpect(jsonPath("$.sizeGradeDistribution.XL").value(10))
                .andExpect(jsonPath("$.largePrRate").value(30.0))
                .andExpect(jsonPath("$.sizeReviewWaitCorrelation.correlationCoefficient").value(0.65))
                .andExpect(jsonPath("$.sizeReviewWaitCorrelation.interpretation").value("다소 강한 양의 상관관계"))
                .andExpect(jsonPath("$.sizeReviewRoundTripCorrelation.correlationCoefficient").value(0.42))
                .andExpect(jsonPath("$.sizeReviewRoundTripCorrelation.interpretation").value("보통 양의 상관관계"));

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
                                fieldWithPath("totalPrCount").description("전체 PR 수"),
                                fieldWithPath("avgSizeScore").description("평균 PR 사이즈 점수"),
                                fieldWithPath("sizeGradeDistribution").description("사이즈 등급별 PR 분포"),
                                fieldWithPath("sizeGradeDistribution.XS").description("Extra Small 등급 PR 수 (0-10)"),
                                fieldWithPath("sizeGradeDistribution.S").description("Small 등급 PR 수 (10-100)"),
                                fieldWithPath("sizeGradeDistribution.M").description("Medium 등급 PR 수 (100-300)"),
                                fieldWithPath("sizeGradeDistribution.L").description("Large 등급 PR 수 (300-1000)"),
                                fieldWithPath("sizeGradeDistribution.XL").description("Extra Large 등급 PR 수 (1000+)"),
                                fieldWithPath("largePrRate").description("Large 이상 PR 비율 (%)"),
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
    @WithOAuth2User(userId = 7L)
    void 데이터가_없으면_빈_통계를_반환한다() throws Exception {
        // given
        given(prSizeStatisticsQueryService.findPrSizeStatistics(eq(7L), eq(1L), any(PrSizeStatisticsRequest.class)))
                .willReturn(PrSizeStatisticsResponse.empty());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/pr-size", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrCount").value(0))
                .andExpect(jsonPath("$.avgSizeScore").value(0.0))
                .andExpect(jsonPath("$.largePrRate").value(0.0));
    }

    @Test
    void 인증_정보가_없으면_PR_사이즈_통계를_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/pr-size", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 소유하지_않은_프로젝트의_통계를_조회하면_404를_반환한다() throws Exception {
        // given
        given(prSizeStatisticsQueryService.findPrSizeStatistics(eq(7L), eq(999L), any(PrSizeStatisticsRequest.class)))
                .willThrow(new ProjectOwnershipException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/statistics/pr-size", 999L)
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
                        get("/projects/{projectId}/statistics/pr-size", 1L)
                                .header("Authorization", "Bearer access-token")
                                .queryParam("startDate", "2024-02-01")
                                .queryParam("endDate", "2024-01-01")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("D03"))
                .andExpect(jsonPath("$.message").value("종료일은 시작일보다 빠를 수 없습니다."));
    }
}
