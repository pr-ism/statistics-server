package com.prism.statistics.application.collect.inbox.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prism.statistics.application.analysis.metadata.review.ReviewerRemovedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerRemovedRequest;
import com.prism.statistics.infrastructure.collect.inbox.CollectInboxType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@SuppressWarnings("NonAsciiCharacters")
@ExtendWith(MockitoExtension.class)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReviewerRemovedInboxHandlerTest {

    @Mock
    ReviewerRemovedService reviewerRemovedService;

    ReviewerRemovedInboxHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        CollectInboxPayloadDeserializer deserializer = new CollectInboxPayloadDeserializer(objectMapper);
        handler = new ReviewerRemovedInboxHandler(deserializer, reviewerRemovedService);
    }

    @Test
    void supportType은_REVIEWER_REMOVED를_반환한다() {
        assertThat(handler.supportType()).isEqualTo(CollectInboxType.REVIEWER_REMOVED);
    }

    @Test
    void payloadJson을_역직렬화하여_서비스를_호출한다() {
        // given
        String payloadJson = """
                {
                    "runId": 100,
                    "githubPullRequestId": 1,
                    "pullRequestNumber": 10,
                    "headCommitSha": "abc123",
                    "reviewer": { "login": "reviewer1", "id": 2 },
                    "removedAt": "2026-03-17T10:00:00Z"
                }
                """;
        CollectInboxContext context = new CollectInboxContext(null, payloadJson);

        // when
        handler.handle(context);

        // then
        ArgumentCaptor<ReviewerRemovedRequest> captor = ArgumentCaptor.forClass(ReviewerRemovedRequest.class);
        verify(reviewerRemovedService).removeReviewer(captor.capture());
        assertThat(captor.getValue().runId()).isEqualTo(100L);
        assertThat(captor.getValue().reviewer().login()).isEqualTo("reviewer1");
    }

    @Test
    void 역직렬화_실패시_IllegalArgumentException을_던진다() {
        // given
        CollectInboxContext context = new CollectInboxContext(null, "{invalid-json}");

        // when & then
        assertThatThrownBy(() -> handler.handle(context))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
