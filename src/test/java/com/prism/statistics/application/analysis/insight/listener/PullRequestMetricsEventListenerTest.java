package com.prism.statistics.application.analysis.insight.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.analysis.metadata.pullrequest.event.PullRequestSavedEvent;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.FileChanges;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedChangeSummaryRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedCommitDensityRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedFileChangeRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestSizeRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestFileRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestMetricsEventListenerTest {

    @Autowired
    private PullRequestMetricsEventListener eventListener;

    @Autowired
    private JpaPullRequestRepository pullRequestRepository;

    @Autowired
    private JpaPullRequestFileRepository pullRequestFileRepository;

    @Autowired
    private JpaPullRequestOpenedChangeSummaryRepository changeSummaryRepository;

    @Autowired
    private JpaPullRequestOpenedCommitDensityRepository commitDensityRepository;

    @Autowired
    private JpaPullRequestOpenedFileChangeRepository fileChangeRepository;

    @Autowired
    private JpaPullRequestSizeRepository pullRequestSizeRepository;

    @Test
    void pull_request_저장_이벤트를_처리하면_파생_지표가_저장된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        createAndSavePullRequestFile(savedPullRequest.getId());
        PullRequestSavedEvent event = new PullRequestSavedEvent(
                savedPullRequest.getGithubPullRequestId(),
                savedPullRequest.getId()
        );

        // when
        eventListener.handle(event);

        // then
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                assertAll(
                        () -> assertThat(changeSummaryRepository.count()).isEqualTo(1),
                        () -> assertThat(commitDensityRepository.count()).isEqualTo(1),
                        () -> assertThat(fileChangeRepository.count()).isEqualTo(1),
                        () -> assertThat(pullRequestSizeRepository.count()).isEqualTo(1)
                )
        );
    }

    private PullRequest createAndSavePullRequest() {
        PullRequest pullRequest = PullRequest.builder()
                .githubPullRequestId(12345L)
                .projectId(10L)
                .author(GithubUser.create("testuser", 1L))
                .pullRequestNumber(1)
                .headCommitSha("abc123")
                .title("Test PR")
                .state(PullRequestState.OPEN)
                .link("https://github.com/test/repo/pull/1")
                .changeStats(PullRequestChangeStats.create(1, 5, 5))
                .commitCount(2)
                .timing(PullRequestTiming.createOpen(LocalDateTime.of(2024, 1, 15, 10, 0)))
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private void createAndSavePullRequestFile(Long pullRequestId) {
        PullRequestFile file = PullRequestFile.create(
                pullRequestId,
                100L,
                "src/main/java/Example.java",
                FileChangeType.MODIFIED,
                FileChanges.create(5, 5)
        );

        pullRequestFileRepository.save(file);
    }
}
