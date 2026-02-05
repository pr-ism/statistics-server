package com.prism.statistics.presentation.collect.review;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prism.statistics.application.analysis.metadata.review.ReviewSubmittedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewSubmittedRequest;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import com.prism.statistics.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

@SuppressWarnings("NonAsciiCharacters")
class ReviewSubmittedControllerTest extends CommonControllerSliceTestSupport {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String TEST_API_KEY = "test-api-key";

    @Autowired
    private ReviewSubmittedService reviewSubmittedService;

    @Test
    void Review_submitted_웹훅_요청을_처리한다() throws Exception {
        // given
        String payload = """
                {
                    "githubPullRequestId": 123456789,
                    "pullRequestNumber": 42,
                    "githubReviewId": 987654321,
                    "reviewer": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "state": "approved",
                    "commitSha": "abc123sha",
                    "body": "LGTM",
                    "commentCount": 3,
                    "submittedAt": "2024-01-15T10:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                post("/collect/review/submitted")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isOk());
    }

    @Test
    void API_Key_헤더_누락_시_400_반환한다() throws Exception {
        // given
        String payload = """
                {
                    "githubPullRequestId": 123456789,
                    "pullRequestNumber": 42,
                    "githubReviewId": 987654321,
                    "reviewer": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "state": "approved",
                    "commitSha": "abc123sha",
                    "body": "LGTM",
                    "commentCount": 3,
                    "submittedAt": "2024-01-15T10:00:00Z"
                }
                """;

        // when & then
        mockMvc.perform(
                post("/collect/review/submitted")
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
                    "githubPullRequestId": 123456789,
                    "pullRequestNumber": 42,
                    "githubReviewId": 987654321,
                    "reviewer": {
                        "login": "reviewer1",
                        "id": 12345
                    },
                    "state": "approved",
                    "commitSha": "abc123sha",
                    "body": "LGTM",
                    "commentCount": 3,
                    "submittedAt": "2024-01-15T10:00:00Z"
                }
                """;

        willThrow(new InvalidApiKeyException())
                .given(reviewSubmittedService).submitReview(eq(TEST_API_KEY), any(ReviewSubmittedRequest.class));

        // when & then
        mockMvc.perform(
                post("/collect/review/submitted")
                        .header(API_KEY_HEADER, TEST_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        )
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.errorCode").value("P01"))
        .andExpect(jsonPath("$.message").value("유효하지 않은 API Key입니다."));
    }
}
