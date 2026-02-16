package com.prism.statistics.domain.analysis.insight.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class CommentAnalysisTest {

    @Test
    void 코멘트_분석을_생성한다() {
        // given
        Long reviewCommentId = 1L;
        Long pullRequestId = 10L;
        String body = "Please fix this issue @john. Here's the code:\n```java\nSystem.out.println(\"test\");\n```";

        // when
        CommentAnalysis analysis = CommentAnalysis.create(reviewCommentId, pullRequestId, body);

        // then
        assertAll(
                () -> assertThat(analysis.getReviewCommentId()).isEqualTo(reviewCommentId),
                () -> assertThat(analysis.getPullRequestId()).isEqualTo(pullRequestId),
                () -> assertThat(analysis.getCommentLength()).isEqualTo(body.length()),
                () -> assertThat(analysis.getLineCount()).isEqualTo(4),
                () -> assertThat(analysis.hasMentions()).isTrue(),
                () -> assertThat(analysis.getMentionCount()).isEqualTo(1),
                () -> assertThat(analysis.isHasCode()).isTrue()
        );
    }

    @Test
    void 코멘트_길이를_계산한다() {
        // given
        String body = "This is a test comment.";

        // when
        CommentAnalysis analysis = CommentAnalysis.create(1L, 1L, body);

        // then
        assertThat(analysis.getCommentLength()).isEqualTo(23);
    }

    @Test
    void 라인_수를_계산한다() {
        // given
        String body = "Line 1\nLine 2\nLine 3";

        // when
        CommentAnalysis analysis = CommentAnalysis.create(1L, 1L, body);

        // then
        assertThat(analysis.getLineCount()).isEqualTo(3);
    }

    @Test
    void Windows_줄바꿈도_처리한다() {
        // given
        String body = "Line 1\r\nLine 2\r\nLine 3";

        // when
        CommentAnalysis analysis = CommentAnalysis.create(1L, 1L, body);

        // then
        assertThat(analysis.getLineCount()).isEqualTo(3);
    }

    @Test
    void 본문이_null이면_길이와_라인수가_0이다() {
        // when
        CommentAnalysis analysis = CommentAnalysis.create(1L, 1L, null);

        // then
        assertAll(
                () -> assertThat(analysis.getCommentLength()).isZero(),
                () -> assertThat(analysis.getLineCount()).isZero()
        );
    }

    @Test
    void 코드_포함_여부를_감지한다() {
        // given
        String withTripleBacktick = "```\ncode\n```";
        String withInlineCode = "Use `console.log()` here";
        String withoutCode = "Just a plain comment";

        CommentAnalysis withBlock = CommentAnalysis.create(1L, 1L, withTripleBacktick);
        CommentAnalysis withInline = CommentAnalysis.create(2L, 1L, withInlineCode);
        CommentAnalysis noCode = CommentAnalysis.create(3L, 1L, withoutCode);

        // then
        assertAll(
                () -> assertThat(withBlock.isHasCode()).isTrue(),
                () -> assertThat(withInline.isHasCode()).isTrue(),
                () -> assertThat(noCode.isHasCode()).isFalse()
        );
    }

    @Test
    void 멀티라인_본문에서_인라인_코드를_감지한다() {
        // given
        String body = "First line\nUse `console.log()` here\nLast line";

        // when
        CommentAnalysis analysis = CommentAnalysis.create(1L, 1L, body);

        // then
        assertThat(analysis.isHasCode()).isTrue();
    }

    @Test
    void 멀티라인_본문에서_URL을_감지한다() {
        // given
        String body = "First line\nCheck https://example.com\nLast line";

        // when
        CommentAnalysis analysis = CommentAnalysis.create(1L, 1L, body);

        // then
        assertThat(analysis.isHasUrl()).isTrue();
    }

    @Test
    void URL_포함_여부를_감지한다() {
        // given
        String withHttps = "Check this: https://example.com";
        String withHttp = "See http://test.org for details";
        String withoutUrl = "No URL here";

        CommentAnalysis httpsComment = CommentAnalysis.create(1L, 1L, withHttps);
        CommentAnalysis httpComment = CommentAnalysis.create(2L, 1L, withHttp);
        CommentAnalysis noUrl = CommentAnalysis.create(3L, 1L, withoutUrl);

        // then
        assertAll(
                () -> assertThat(httpsComment.isHasUrl()).isTrue(),
                () -> assertThat(httpComment.isHasUrl()).isTrue(),
                () -> assertThat(noUrl.isHasUrl()).isFalse()
        );
    }

    @Test
    void Review_Comment_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CommentAnalysis.create(null, 1L, "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Review Comment ID는 필수입니다.");
    }

    @Test
    void Pull_Request_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> CommentAnalysis.create(1L, null, "test"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Pull Request ID는 필수입니다.");
    }

    @Test
    void 짧은_코멘트_여부를_확인한다() {
        // given
        String shortComment = "LGTM";
        String longComment = "This looks good to me, but please consider adding more tests for edge cases.";

        CommentAnalysis shortAnalysis = CommentAnalysis.create(1L, 1L, shortComment);
        CommentAnalysis longAnalysis = CommentAnalysis.create(2L, 1L, longComment);

        // then
        assertAll(
                () -> assertThat(shortAnalysis.isShortComment()).isTrue(),
                () -> assertThat(longAnalysis.isShortComment()).isFalse()
        );
    }

    @Test
    void 상세_코멘트_여부를_확인한다() {
        // given
        String detailedComment = "This is a very detailed comment that explains the issue thoroughly. " +
                "It provides context about why the change is needed and suggests specific improvements. " +
                "Additionally, it includes code examples and references to documentation. " +
                "This should help the author understand exactly what needs to be changed.";
        String briefComment = "Please fix this.";

        CommentAnalysis detailed = CommentAnalysis.create(1L, 1L, detailedComment);
        CommentAnalysis brief = CommentAnalysis.create(2L, 1L, briefComment);

        // then
        assertAll(
                () -> assertThat(detailed.isDetailedComment()).isTrue(),
                () -> assertThat(brief.isDetailedComment()).isFalse()
        );
    }

    @Test
    void 풍부한_코멘트_여부를_확인한다() {
        // given
        String withCode = "Use `this.method()` instead";
        String withUrl = "See https://docs.example.com";
        String plain = "Please fix this";

        CommentAnalysis codeComment = CommentAnalysis.create(1L, 1L, withCode);
        CommentAnalysis urlComment = CommentAnalysis.create(2L, 1L, withUrl);
        CommentAnalysis plainComment = CommentAnalysis.create(3L, 1L, plain);

        // then
        assertAll(
                () -> assertThat(codeComment.isRichComment()).isTrue(),
                () -> assertThat(urlComment.isRichComment()).isTrue(),
                () -> assertThat(plainComment.isRichComment()).isFalse()
        );
    }

    @Test
    void 멘션_수를_반환한다() {
        // given
        String body = "@john @jane @bob please review";

        // when
        CommentAnalysis analysis = CommentAnalysis.create(1L, 1L, body);

        // then
        assertThat(analysis.getMentionCount()).isEqualTo(3);
    }

    @Test
    void 본문이_수정되면_모든_분석_결과가_갱신된다() {
        // given
        String originalBody = "Please fix this @john";
        CommentAnalysis analysis = CommentAnalysis.create(1L, 10L, originalBody);

        String updatedBody = "Updated comment with ```code``` and https://example.com @jane @bob";

        // when
        analysis.updateBody(updatedBody);

        // then
        assertAll(
                () -> assertThat(analysis.getReviewCommentId()).isEqualTo(1L),
                () -> assertThat(analysis.getPullRequestId()).isEqualTo(10L),
                () -> assertThat(analysis.getCommentLength()).isEqualTo(updatedBody.length()),
                () -> assertThat(analysis.getLineCount()).isEqualTo(1),
                () -> assertThat(analysis.getMentionCount()).isEqualTo(2),
                () -> assertThat(analysis.isHasCode()).isTrue(),
                () -> assertThat(analysis.isHasUrl()).isTrue()
        );
    }

    @Test
    void 본문이_수정되어_멘션이_제거되면_멘션_수가_0이_된다() {
        // given
        String originalBody = "@john @jane please review";
        CommentAnalysis analysis = CommentAnalysis.create(1L, 10L, originalBody);

        String updatedBody = "please review";

        // when
        analysis.updateBody(updatedBody);

        // then
        assertAll(
                () -> assertThat(analysis.hasMentions()).isFalse(),
                () -> assertThat(analysis.getMentionCount()).isZero()
        );
    }

    @Test
    void 멘션이_없으면_멘션_수가_0이다() {
        // given
        String body = "Please review this change";

        // when
        CommentAnalysis analysis = CommentAnalysis.create(1L, 1L, body);

        // then
        assertAll(
                () -> assertThat(analysis.hasMentions()).isFalse(),
                () -> assertThat(analysis.getMentionCount()).isZero()
        );
    }
}
