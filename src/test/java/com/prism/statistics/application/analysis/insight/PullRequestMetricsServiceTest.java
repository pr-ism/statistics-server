package com.prism.statistics.application.analysis.insight;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import com.prism.statistics.domain.analysis.metadata.common.vo.GithubUser;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.PullRequestState;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.FileChanges;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestTiming;
import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.enums.SizeGrade;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedChangeSummaryRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedCommitDensityRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestOpenedFileChangeRepository;
import com.prism.statistics.infrastructure.analysis.insight.persistence.JpaPullRequestSizeRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestFileRepository;
import com.prism.statistics.infrastructure.analysis.metadata.pullrequest.persistence.JpaPullRequestRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PullRequestMetricsServiceTest {

    @Autowired
    private PullRequestMetricsService metricsService;

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
    void 존재하지_않는_PR로_메트릭을_생성하면_예외가_발생한다() {
        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                metricsService.deriveMetrics(999999L)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PullRequest not found");
    }

    @Test
    void pull_request_id로_메트릭을_생성하면_세_종류_파생_지표가_저장된다() {
        // given
        PullRequest savedPullRequest = createAndSavePullRequest();
        createAndSavePullRequestFiles(savedPullRequest.getId());

        // when
        metricsService.deriveMetrics(savedPullRequest.getId());

        // then
        List<PullRequestOpenedChangeSummary> changeSummaries = changeSummaryRepository.findAll();
        List<PullRequestOpenedCommitDensity> commitDensities = commitDensityRepository.findAll();
        List<PullRequestOpenedFileChange> fileChanges = fileChangeRepository.findAll();
        List<PullRequestSize> pullRequestSizes = pullRequestSizeRepository.findAll();

        assertAll(
                () -> assertThat(changeSummaries)
                        .singleElement()
                        .satisfies(summary -> assertAll(
                                () -> assertThat(summary.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                                () -> assertThat(summary.getTotalChanges()).isEqualTo(16),
                                () -> assertThat(summary.getAvgChangesPerFile()).isEqualTo(new BigDecimal("8.0000"))
                        )),
                () -> assertThat(commitDensities)
                        .singleElement()
                        .satisfies(density -> assertAll(
                                () -> assertThat(density.getCommitDensityPerFile()).isEqualTo(new BigDecimal("2.0000")),
                                () -> assertThat(density.getCommitDensityPerChange()).isEqualTo(new BigDecimal("0.250000"))
                        )),
                () -> assertThat(fileChanges)
                        .hasSize(2)
                        .anySatisfy(item -> assertAll(
                                () -> assertThat(item.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                                () -> assertThat(item.getChangeType()).isEqualTo(FileChangeType.MODIFIED),
                                () -> assertThat(item.getCount()).isEqualTo(1),
                                () -> assertThat(item.getRatio()).isEqualTo(new BigDecimal("0.50"))
                        )),
                () -> assertThat(fileChanges)
                        .hasSize(2)
                        .anySatisfy(item -> assertAll(
                                () -> assertThat(item.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                                () -> assertThat(item.getChangeType()).isEqualTo(FileChangeType.ADDED),
                                () -> assertThat(item.getCount()).isEqualTo(1),
                                () -> assertThat(item.getRatio()).isEqualTo(new BigDecimal("0.50"))
                        )),
                () -> assertThat(pullRequestSizes)
                        .singleElement()
                        .satisfies(size -> assertAll(
                                () -> assertThat(size.getPullRequestId()).isEqualTo(savedPullRequest.getId()),
                                () -> assertThat(size.getAdditionCount()).isEqualTo(10),
                                () -> assertThat(size.getDeletionCount()).isEqualTo(6),
                                () -> assertThat(size.getChangedFileCount()).isEqualTo(2),
                                () -> assertThat(size.getSizeScore()).isEqualByComparingTo(new BigDecimal("18")),
                                () -> assertThat(size.getSizeGrade()).isEqualTo(SizeGrade.S),
                                () -> assertThat(size.getFileChangeDiversity()).isEqualByComparingTo(new BigDecimal("0.5"))
                        ))
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
                .changeStats(PullRequestChangeStats.create(2, 10, 6))
                .commitCount(4)
                .timing(PullRequestTiming.createOpen(LocalDateTime.of(2024, 1, 15, 10, 0)))
                .build();

        return pullRequestRepository.save(pullRequest);
    }

    private void createAndSavePullRequestFiles(Long pullRequestId) {
        List<PullRequestFile> files = List.of(
                PullRequestFile.create(
                        pullRequestId,
                        "src/main/java/Example.java",
                        FileChangeType.MODIFIED,
                        FileChanges.create(8, 2)
                ),
                PullRequestFile.create(
                        pullRequestId,
                        "src/main/java/NewFile.java",
                        FileChangeType.ADDED,
                        FileChanges.create(2, 4)
                )
        );

        pullRequestFileRepository.saveAll(files);
    }
}
