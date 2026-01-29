package com.prism.statistics.presentation.pullrequest;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.pullrequest.PullRequestQueryService;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestDetailResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestDetailResponse.ChangeStatsResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestDetailResponse.TimingResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestListResponse;
import com.prism.statistics.application.pullrequest.dto.response.PullRequestListResponse.PullRequestSummary;
import com.prism.statistics.context.security.WithOAuth2User;
import com.prism.statistics.infrastructure.project.persistence.exception.ProjectNotFoundException;
import com.prism.statistics.infrastructure.pullrequest.persistence.exception.PullRequestNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class PullRequestControllerTest extends CommonControllerSliceTestSupport {

    @Autowired
    private PullRequestQueryService pullRequestQueryService;

    @Test
    @WithOAuth2User(userId = 7L)
    void PR_목록_조회_성공_테스트() throws Exception {
        // given
        PullRequestListResponse response = new PullRequestListResponse(
                List.of(
                        new PullRequestSummary(1L, 30, "세 번째 PR", "CLOSED", "author1", "https://github.com/test/repo/pull/30"),
                        new PullRequestSummary(2L, 20, "두 번째 PR", "MERGED", "author2", "https://github.com/test/repo/pull/20"),
                        new PullRequestSummary(3L, 10, "첫 번째 PR", "OPEN", "author1", "https://github.com/test/repo/pull/10")
                )
        );

        given(pullRequestQueryService.findAllByProjectId(7L, 1L)).willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/pull-requests", 1L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pullRequests").isArray())
                .andExpect(jsonPath("$.pullRequests.length()").value(3))
                .andExpect(jsonPath("$.pullRequests[0].prNumber").value(30))
                .andExpect(jsonPath("$.pullRequests[1].prNumber").value(20))
                .andExpect(jsonPath("$.pullRequests[2].prNumber").value(10));

        PR_목록_조회_문서화(resultActions);
    }

    private void PR_목록_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID")
                        ),
                        responseFields(
                                fieldWithPath("pullRequests").description("PR 목록"),
                                fieldWithPath("pullRequests[].id").description("PR 식별자"),
                                fieldWithPath("pullRequests[].prNumber").description("GitHub PR 번호"),
                                fieldWithPath("pullRequests[].title").description("PR 제목"),
                                fieldWithPath("pullRequests[].state").description("PR 상태 (OPEN, MERGED, CLOSED, DRAFT)"),
                                fieldWithPath("pullRequests[].authorGithubId").description("작성자 GitHub ID"),
                                fieldWithPath("pullRequests[].link").description("PR 링크")
                        )
                )
        );
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void PR_상세_조회_성공_테스트() throws Exception {
        // given
        PullRequestDetailResponse response = new PullRequestDetailResponse(
                2L,
                20,
                "두 번째 PR",
                "MERGED",
                "author2",
                "https://github.com/test/repo/pull/20",
                4,
                new ChangeStatsResponse(5, 100, 30),
                new TimingResponse(
                        LocalDateTime.of(2024, 1, 10, 9, 0, 0),
                        LocalDateTime.of(2024, 1, 12, 15, 0, 0),
                        LocalDateTime.of(2024, 1, 12, 15, 0, 0)
                )
        );

        given(pullRequestQueryService.findByProjectIdAndPrNumber(7L, 1L, 20)).willReturn(response);

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        get("/projects/{projectId}/pull-requests/{prNumber}", 1L, 20)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prNumber").value(20))
                .andExpect(jsonPath("$.title").value("두 번째 PR"))
                .andExpect(jsonPath("$.state").value("MERGED"))
                .andExpect(jsonPath("$.commitCount").value(4))
                .andExpect(jsonPath("$.changeStats.changedFileCount").value(5))
                .andExpect(jsonPath("$.changeStats.additionCount").value(100))
                .andExpect(jsonPath("$.changeStats.deletionCount").value(30))
                .andExpect(jsonPath("$.timing.mergedAt").exists());

        PR_상세_조회_문서화(resultActions);
    }

    private void PR_상세_조회_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("Authorization").description("Access Token 값")
                        ),
                        pathParameters(
                                parameterWithName("projectId").description("프로젝트 ID"),
                                parameterWithName("prNumber").description("GitHub PR 번호")
                        ),
                        responseFields(
                                fieldWithPath("id").description("PR 식별자"),
                                fieldWithPath("prNumber").description("GitHub PR 번호"),
                                fieldWithPath("title").description("PR 제목"),
                                fieldWithPath("state").description("PR 상태 (OPEN, MERGED, CLOSED, DRAFT)"),
                                fieldWithPath("authorGithubId").description("작성자 GitHub ID"),
                                fieldWithPath("link").description("PR 링크"),
                                fieldWithPath("commitCount").description("커밋 수"),
                                fieldWithPath("changeStats").description("변경 통계"),
                                fieldWithPath("changeStats.changedFileCount").description("변경된 파일 수"),
                                fieldWithPath("changeStats.additionCount").description("추가된 라인 수"),
                                fieldWithPath("changeStats.deletionCount").description("삭제된 라인 수"),
                                fieldWithPath("timing").description("시간 정보"),
                                fieldWithPath("timing.prCreatedAt").description("PR 생성 시각"),
                                fieldWithPath("timing.mergedAt").description("병합 시각").optional(),
                                fieldWithPath("timing.closedAt").description("종료 시각").optional()
                        )
                )
        );
    }

    @Test
    void 인증_정보가_없으면_PR_목록을_조회할_수_없다() throws Exception {
        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/pull-requests", 1L)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("A04"))
                .andExpect(jsonPath("$.message").value("인가되지 않은 회원"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 존재하지_않는_프로젝트의_PR_목록을_조회하면_404를_반환한다() throws Exception {
        // given
        given(pullRequestQueryService.findAllByProjectId(7L, 999L))
                .willThrow(new ProjectNotFoundException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/pull-requests", 999L)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P00"));
    }

    @Test
    @WithOAuth2User(userId = 7L)
    void 존재하지_않는_PR을_조회하면_404를_반환한다() throws Exception {
        // given
        given(pullRequestQueryService.findByProjectIdAndPrNumber(7L, 1L, 999))
                .willThrow(new PullRequestNotFoundException());

        // when & then
        mockMvc.perform(
                        get("/projects/{projectId}/pull-requests/{prNumber}", 1L, 999)
                                .header("Authorization", "Bearer access-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PR00"));
    }
}
