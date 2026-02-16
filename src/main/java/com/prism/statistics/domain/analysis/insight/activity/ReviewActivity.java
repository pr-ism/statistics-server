package com.prism.statistics.domain.analysis.insight.activity;

import com.prism.statistics.domain.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "review_activities")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewActivity extends BaseTimeEntity {

    private static final int DENSITY_SCALE = 6;

    private Long pullRequestId;

    private int reviewRoundTrips;

    private int totalCommentCount;

    private BigDecimal commentDensity;

    private int codeAdditionsAfterReview;

    private int codeDeletionsAfterReview;

    private boolean hasAdditionalReviewers;

    private int additionalReviewerCount;

    private int totalAdditions;

    private int totalDeletions;

    public static ReviewActivity createWithoutReview(
            Long pullRequestId,
            int totalAdditions,
            int totalDeletions
    ) {
        return ReviewActivity.builder()
                .pullRequestId(pullRequestId)
                .reviewRoundTrips(0)
                .totalCommentCount(0)
                .codeAdditionsAfterReview(0)
                .codeDeletionsAfterReview(0)
                .additionalReviewerCount(0)
                .totalAdditions(totalAdditions)
                .totalDeletions(totalDeletions)
                .build();
    }

    private BigDecimal calculateCommentDensity(int commentCount, int additions, int deletions) {
        int totalChanges = additions + deletions;
        if (totalChanges == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(commentCount)
                .divide(BigDecimal.valueOf(totalChanges), DENSITY_SCALE, RoundingMode.HALF_UP);
    }

    private void validatePullRequestId(Long pullRequestId) {
        if (pullRequestId == null) {
            throw new IllegalArgumentException("Pull Request ID는 필수입니다.");
        }
    }

    private void validateNonNegative(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("값은 0보다 작을 수 없습니다.");
        }
    }

    @Builder
    private ReviewActivity(
            Long pullRequestId,
            int reviewRoundTrips,
            int totalCommentCount,
            int codeAdditionsAfterReview,
            int codeDeletionsAfterReview,
            int additionalReviewerCount,
            int totalAdditions,
            int totalDeletions
    ) {
        validatePullRequestId(pullRequestId);
        validateNonNegative(reviewRoundTrips);
        validateNonNegative(totalCommentCount);
        validateNonNegative(totalAdditions);
        validateNonNegative(totalDeletions);
        validateNonNegative(additionalReviewerCount);
        validateNonNegative(codeAdditionsAfterReview);
        validateNonNegative(codeDeletionsAfterReview);
        this.pullRequestId = pullRequestId;
        this.reviewRoundTrips = reviewRoundTrips;
        this.totalCommentCount = totalCommentCount;
        this.commentDensity = calculateCommentDensity(totalCommentCount, totalAdditions, totalDeletions);
        this.codeAdditionsAfterReview = codeAdditionsAfterReview;
        this.codeDeletionsAfterReview = codeDeletionsAfterReview;
        this.hasAdditionalReviewers = additionalReviewerCount > 0;
        this.additionalReviewerCount = additionalReviewerCount;
        this.totalAdditions = totalAdditions;
        this.totalDeletions = totalDeletions;
    }

    public void updateOnNewReview(int newCommentCount) {
        validateNonNegative(newCommentCount);
        this.reviewRoundTrips++;
        this.totalCommentCount += newCommentCount;
        this.commentDensity = calculateCommentDensity(this.totalCommentCount, this.totalAdditions, this.totalDeletions);
    }

    public void updateCodeChangesAfterReview(int additions, int deletions) {
        validateNonNegative(additions);
        validateNonNegative(deletions);
        this.codeAdditionsAfterReview += additions;
        this.codeDeletionsAfterReview += deletions;
    }

    public void updateOnReviewerAdded() {
        this.additionalReviewerCount++;
        this.hasAdditionalReviewers = true;
    }

    public void updateTotalChanges(int newTotalAdditions, int newTotalDeletions) {
        validateNonNegative(newTotalAdditions);
        validateNonNegative(newTotalDeletions);
        this.totalAdditions = newTotalAdditions;
        this.totalDeletions = newTotalDeletions;
        this.commentDensity = calculateCommentDensity(this.totalCommentCount, this.totalAdditions, this.totalDeletions);
    }

    public int calculateTotalCodeChangesAfterReview() {
        return codeAdditionsAfterReview + codeDeletionsAfterReview;
    }

    public int calculateTotalChanges() {
        return totalAdditions + totalDeletions;
    }

    public boolean hasReviewActivity() {
        return reviewRoundTrips > 0;
    }

    public boolean hasHighCommentDensity() {
        return commentDensity.compareTo(BigDecimal.valueOf(0.1)) >= 0;
    }

    public boolean hasSignificantChangesAfterReview() {
        return calculateTotalCodeChangesAfterReview() >= 10;
    }
}
