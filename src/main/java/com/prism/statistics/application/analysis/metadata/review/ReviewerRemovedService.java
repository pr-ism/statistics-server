package com.prism.statistics.application.analysis.metadata.review;

import com.prism.statistics.application.analysis.metadata.review.dto.request.ReviewerRemovedRequest;
import com.prism.statistics.application.analysis.metadata.utils.LocalDateTimeConverter;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.analysis.metadata.review.history.RequestedReviewerHistory;
import com.prism.statistics.domain.analysis.metadata.review.enums.ReviewerAction;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerHistoryRepository;
import com.prism.statistics.domain.analysis.metadata.review.repository.RequestedReviewerRepository;
import com.prism.statistics.infrastructure.project.persistence.exception.InvalidApiKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewerRemovedService {

    private final LocalDateTimeConverter localDateTimeConverter;
    private final ProjectRepository projectRepository;
    private final RequestedReviewerRepository requestedReviewerRepository;
    private final RequestedReviewerHistoryRepository requestedReviewerHistoryRepository;

    @Transactional
    public void removeReviewer(String apiKey, ReviewerRemovedRequest request) {
        if (!projectRepository.existsByApiKey(apiKey)) {
            throw new InvalidApiKeyException();
        }

        Long githubPullRequestId = request.githubPullRequestId();
        Long userId = request.reviewer().id();

        long deleted = requestedReviewerRepository.deleteByGithubId(githubPullRequestId, userId);

        if (deleted == 0L) {
            return;
        }

        GithubUser reviewer = GithubUser.create(request.reviewer().login(), request.reviewer().id());
        LocalDateTime removedAt = localDateTimeConverter.toLocalDateTime(request.removedAt());

        RequestedReviewerHistory requestedReviewerHistory = RequestedReviewerHistory.create(
                githubPullRequestId,
                request.headCommitSha(),
                reviewer,
                ReviewerAction.REMOVED,
                removedAt
        );

        requestedReviewerHistoryRepository.save(requestedReviewerHistory);
    }
}
