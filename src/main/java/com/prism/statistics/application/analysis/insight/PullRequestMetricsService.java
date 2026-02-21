package com.prism.statistics.application.analysis.insight;

import com.prism.statistics.domain.analysis.insight.PullRequestOpenedChangeSummary;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedCommitDensity;
import com.prism.statistics.domain.analysis.insight.PullRequestOpenedFileChange;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedChangeSummaryRepository;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedCommitDensityRepository;
import com.prism.statistics.domain.analysis.insight.repository.PullRequestOpenedFileChangeRepository;
import com.prism.statistics.domain.analysis.insight.size.PullRequestSize;
import com.prism.statistics.domain.analysis.insight.size.repository.PullRequestSizeRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequest;
import com.prism.statistics.domain.analysis.metadata.pullrequest.PullRequestFile;
import com.prism.statistics.domain.analysis.metadata.pullrequest.enums.FileChangeType;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestFileRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.repository.PullRequestRepository;
import com.prism.statistics.domain.analysis.metadata.pullrequest.vo.PullRequestChangeStats;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PullRequestMetricsService {

    private final PullRequestRepository pullRequestRepository;
    private final PullRequestFileRepository pullRequestFileRepository;
    private final PullRequestOpenedFileChangeRepository fileChangeRepository;
    private final PullRequestOpenedChangeSummaryRepository changeSummaryRepository;
    private final PullRequestOpenedCommitDensityRepository commitDensityRepository;
    private final PullRequestSizeRepository pullRequestSizeRepository;

    @Transactional
    public void deriveMetrics(Long pullRequestId) {
        PullRequest pullRequest = pullRequestRepository.findById(pullRequestId)
                .orElseThrow(() -> new IllegalArgumentException("PullRequest not found: " + pullRequestId));
        List<PullRequestFile> files = pullRequestFileRepository.findAllByPullRequestId(pullRequestId);
        Map<FileChangeType, Integer> fileTypeCounts = countFileChangeTypes(files);

        PullRequestOpenedChangeSummary changeSummary = createChangeSummary(pullRequest);
        PullRequestOpenedCommitDensity commitDensity = createCommitDensity(pullRequest);
        List<PullRequestOpenedFileChange> fileChanges = createFileChanges(pullRequestId, fileTypeCounts);
        PullRequestSize pullRequestSize = createPullRequestSize(pullRequest, fileTypeCounts);

        changeSummaryRepository.save(changeSummary);
        commitDensityRepository.save(commitDensity);
        fileChangeRepository.saveAll(fileChanges);
        pullRequestSizeRepository.save(pullRequestSize);
    }

    private PullRequestOpenedChangeSummary createChangeSummary(PullRequest pullRequest) {
        PullRequestChangeStats stats = pullRequest.getChangeStats();
        int totalChanges = stats.getAdditionCount() + stats.getDeletionCount();
        BigDecimal avgChangesPerFile = divideOrZero(totalChanges, stats.getChangedFileCount(), 4);

        return PullRequestOpenedChangeSummary.create(
                pullRequest.getId(),
                totalChanges,
                avgChangesPerFile
        );
    }

    private PullRequestOpenedCommitDensity createCommitDensity(PullRequest pullRequest) {
        PullRequestChangeStats stats = pullRequest.getChangeStats();
        int totalChanges = stats.getAdditionCount() + stats.getDeletionCount();

        BigDecimal densityPerFile = divideOrZero(pullRequest.getCommitCount(), stats.getChangedFileCount(), 4);
        BigDecimal densityPerChange = divideOrZero(pullRequest.getCommitCount(), totalChanges, 6);

        return PullRequestOpenedCommitDensity.create(
                pullRequest.getId(),
                densityPerFile,
                densityPerChange
        );
    }

    private List<PullRequestOpenedFileChange> createFileChanges(
            Long pullRequestId,
            Map<FileChangeType, Integer> typeCounts
    ) {
        int total = typeCounts.values().stream().mapToInt(i -> i).sum();
        if (total == 0) {
            return List.of();
        }

        return typeCounts.entrySet()
                         .stream()
                         .filter(entry -> entry.getValue() > 0)
                         .map(entry -> PullRequestOpenedFileChange.create(
                                 pullRequestId,
                                 entry.getKey(),
                                 entry.getValue(),
                                 calculateRatio(entry.getValue(), total)
                         ))
                         .toList();
    }

    private PullRequestSize createPullRequestSize(
            PullRequest pullRequest,
            Map<FileChangeType, Integer> fileTypeCounts
    ) {
        PullRequestChangeStats stats = pullRequest.getChangeStats();
        BigDecimal fileChangeDiversity = PullRequestSize.calculateFileChangeDiversity(
                fileTypeCounts.getOrDefault(FileChangeType.ADDED, 0),
                fileTypeCounts.getOrDefault(FileChangeType.MODIFIED, 0),
                fileTypeCounts.getOrDefault(FileChangeType.REMOVED, 0),
                fileTypeCounts.getOrDefault(FileChangeType.RENAMED, 0)
        );

        return PullRequestSize.create(
                pullRequest.getId(),
                stats.getAdditionCount(),
                stats.getDeletionCount(),
                stats.getChangedFileCount(),
                fileChangeDiversity
        );
    }

    private Map<FileChangeType, Integer> countFileChangeTypes(List<PullRequestFile> files) {
        Map<FileChangeType, Integer> counts = new EnumMap<>(FileChangeType.class);
        for (FileChangeType type : FileChangeType.values()) {
            counts.put(type, 0);
        }
        for (PullRequestFile file : files) {
            FileChangeType type = file.getChangeType();
            counts.put(type, counts.get(type) + 1);
        }
        return counts;
    }

    private BigDecimal divideOrZero(int numerator, int denominator, int scale) {
        if (denominator == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(numerator)
                         .divide(BigDecimal.valueOf(denominator), scale, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateRatio(int count, int total) {
        return divideOrZero(count, total, 2);
    }
}
