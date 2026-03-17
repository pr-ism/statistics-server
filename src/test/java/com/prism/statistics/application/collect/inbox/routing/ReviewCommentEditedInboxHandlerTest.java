package com.prism.statistics.application.collect.inbox.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.prism.statistics.application.analysis.metadata.review.ReviewCommentEditedService;
import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewCommentEditedRequest;
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
class ReviewCommentEditedInboxHandlerTest {

    @Mock
    ReviewCommentEditedService reviewCommentEditedService;

    ReviewCommentEditedInboxHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        handler = new ReviewCommentEditedInboxHandler(objectMapper, reviewCommentEditedService);
    }

    @Test
    void supportType은_REVIEW_COMMENT_EDITED를_반환한다() {
        assertThat(handler.supportType()).isEqualTo(CollectInboxType.REVIEW_COMMENT_EDITED);
    }

    @Test
    void payloadJson을_역직렬화하여_서비스를_호출한다() {
        // given
        String payloadJson = """
                {
                    "runId": 100,
                    "githubCommentId": 300,
                    "body": "수정된 코멘트",
                    "updatedAt": "2026-03-17T10:00:00Z"
                }
                """;
        CollectInboxContext context = new CollectInboxContext(null, payloadJson);

        // when
        handler.handle(context);

        // then
        ArgumentCaptor<ReviewCommentEditedRequest> captor = ArgumentCaptor.forClass(ReviewCommentEditedRequest.class);
        verify(reviewCommentEditedService).editReviewComment(captor.capture());
        assertThat(captor.getValue().githubCommentId()).isEqualTo(300L);
    }

    @Test
    void 역직렬화_실패시_RuntimeException으로_감싸서_던진다() {
        // given
        CollectInboxContext context = new CollectInboxContext(null, "{invalid-json}");

        // when & then
        assertThatThrownBy(() -> handler.handle(context))
                .isInstanceOf(RuntimeException.class);
    }
}
