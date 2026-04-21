package com.prism.statistics.presentation.collect.review.comment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.analysis.metadata.review.ReviewCommentCreatedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentCreatedRequest;
import com.prism.statistics.application.collect.ProjectApiKeyService;
import com.prism.statistics.domain.project.exception.InvalidApiKeyException;
import com.prism.statistics.domain.analysis.metadata.review.exception.ReviewCommentNotFoundException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

@SuppressWarnings("NonAsciiCharacters")
class ReviewCommentCreatedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ProjectApiKeyService projectApiKeyService;

    @Autowired
    private ReviewCommentCreatedService reviewCommentCreatedService;

    @Test
    void Review_comment_created_이벤트_수집_성공_테스트() throws Exception {
        // given
        String payload = """
                {
                    "runId": 12345,
                    "githubCommentId": 123456789,
                    "githubReviewId": 987654321,
                    "body": "코드 리뷰 댓글입니다.",
                    "path": "src/main/java/Example.java",
                    "line": 10,
                    "startLine": null,
                    "side": "right",
                    "commitSha": "abc123sha",
                    "inReplyToId": null,
                    "author": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "createdAt": "2024-01-15T10:00:00Z"
                }
                """;

        // when & then
        ResultActions resultActions = mockMvc.perform(
                        post("/collect/review/comment/created")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNoContent());

        then(reviewCommentCreatedService).should()
                .createReviewComment(any(ReviewCommentCreatedRequest.class));

        Review_comment_created_이벤트_수집_문서화(resultActions);
    }

    private void Review_comment_created_이벤트_수집_문서화(ResultActions resultActions) throws Exception {
        resultActions.andDo(
                restDocs.document(
                        requestHeaders(
                                headerWithName("X-API-Key").description("프로젝트 API Key")
                        ),
                        requestFields(
                                fieldWithPath("runId").description("GitHub Actions Run ID"),
                                fieldWithPath("githubCommentId").description("GitHub Comment ID"),
                                fieldWithPath("githubReviewId").description("GitHub Review ID").optional(),
                                fieldWithPath("body").description("코멘트 내용").optional(),
                                fieldWithPath("path").description("파일 경로").optional(),
                                fieldWithPath("line").description("라인 번호"),
                                fieldWithPath("startLine").description("시작 라인 번호 (멀티라인 코멘트)").optional(),
                                fieldWithPath("side").description("코멘트 위치 (left, right)").optional(),
                                fieldWithPath("commitSha").description("커밋 SHA").optional(),
                                fieldWithPath("inReplyToId").description("답글 대상 코멘트 ID").optional(),
                                fieldWithPath("author").description("작성자 정보"),
                                fieldWithPath("author.login").description("작성자 GitHub 로그인"),
                                fieldWithPath("author.id").description("작성자 GitHub ID").optional(),
                                fieldWithPath("createdAt").description("생성 일시")
                        )
                )
        );
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "githubCommentId": 123456789,
                    "githubReviewId": 987654321,
                    "body": "코드 리뷰 댓글입니다.",
                    "path": "src/main/java/Example.java",
                    "line": 10,
                    "startLine": null,
                    "side": "right",
                    "commitSha": "abc123sha",
                    "inReplyToId": null,
                    "author": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "createdAt": "2024-01-15T10:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                        post("/collect/review/comment/created")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void 유효하지_않은_API_Key면_404_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "githubCommentId": 123456789,
                    "githubReviewId": 987654321,
                    "body": "코드 리뷰 댓글입니다.",
                    "path": "src/main/java/Example.java",
                    "line": 10,
                    "startLine": null,
                    "side": "right",
                    "commitSha": "abc123sha",
                    "inReplyToId": null,
                    "author": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "createdAt": "2024-01-15T10:00:00Z"
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(projectApiKeyService).validateApiKey(TEST_API_KEY);

        // when & then
        mockMvc.perform(
                        post("/collect/review/comment/created")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P01"))
                .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }

    @Test
    void ReviewComment를_찾을_수_없으면_404_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "githubCommentId": 123456789,
                    "githubReviewId": 987654321,
                    "body": "코드 리뷰 댓글입니다.",
                    "path": "src/main/java/Example.java",
                    "line": 10,
                    "startLine": null,
                    "side": "right",
                    "commitSha": "abc123sha",
                    "inReplyToId": null,
                    "author": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "createdAt": "2024-01-15T10:00:00Z"
                }
                """;

        willThrow(new ReviewCommentNotFoundException())
                .given(reviewCommentCreatedService).createReviewComment(any(ReviewCommentCreatedRequest.class));

        // when & then
        mockMvc.perform(
                        post("/collect/review/comment/created")
                                .header(API_KEY_HEADER, TEST_API_KEY)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.errorCode").value("RC00"))
                .andExpect(jsonPath("$.message").value("리뷰 댓글을 찾을 수 없습니다."));
    }
}
