package com.prism.statistics.application.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.metric.dto.request.HotFileStatisticsRequest;
import com.prism.statistics.application.metric.dto.response.HotFileStatisticsResponse;
import com.prism.statistics.application.metric.dto.response.HotFileStatisticsResponse.HotFileStatistics;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class HotFileStatisticsQueryServiceTest {

    @Autowired
    private HotFileStatisticsQueryService hotFileStatisticsQueryService;

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_hot_file_statistics.sql")
    @Test
    void 프로젝트의_핫_파일_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        HotFileStatisticsRequest request = new HotFileStatisticsRequest(null, null, null);

        // when
        HotFileStatisticsResponse actual = hotFileStatisticsQueryService.findHotFileStatistics(userId, projectId, request);

        // then
        // Application.java: 3회 (PR1 MODIFIED, PR2 MODIFIED, PR3 MODIFIED)
        // Config.java: 2회 (PR1 ADDED, PR2 MODIFIED)
        // Service.java: 2회 (PR2 ADDED, PR3 RENAMED)
        // README.md: 1회 (PR1 MODIFIED)
        // OldFile.java: 1회 (PR2 REMOVED)
        HotFileStatistics applicationJava = actual.hotFiles().stream()
                .filter(f -> f.fileName().equals("src/main/java/Application.java"))
                .findFirst()
                .orElseThrow();

        HotFileStatistics configJava = actual.hotFiles().stream()
                .filter(f -> f.fileName().equals("src/main/java/Config.java"))
                .findFirst()
                .orElseThrow();

        HotFileStatistics serviceJava = actual.hotFiles().stream()
                .filter(f -> f.fileName().equals("src/main/java/Service.java"))
                .findFirst()
                .orElseThrow();

        assertAll(
                () -> assertThat(actual.hotFiles()).hasSize(5),
                () -> assertThat(actual.hotFiles().get(0).fileName()).isEqualTo("src/main/java/Application.java"),
                () -> assertThat(applicationJava.changeCount()).isEqualTo(3),
                () -> assertThat(applicationJava.totalAdditions()).isEqualTo(130),
                () -> assertThat(applicationJava.totalDeletions()).isEqualTo(45),
                () -> assertThat(applicationJava.modifiedCount()).isEqualTo(3),
                () -> assertThat(applicationJava.addedCount()).isEqualTo(0),
                () -> assertThat(applicationJava.removedCount()).isEqualTo(0),
                () -> assertThat(applicationJava.renamedCount()).isEqualTo(0),
                () -> assertThat(configJava.changeCount()).isEqualTo(2),
                () -> assertThat(configJava.totalAdditions()).isEqualTo(75),
                () -> assertThat(configJava.totalDeletions()).isEqualTo(10),
                () -> assertThat(configJava.modifiedCount()).isEqualTo(1),
                () -> assertThat(configJava.addedCount()).isEqualTo(1),
                () -> assertThat(serviceJava.changeCount()).isEqualTo(2),
                () -> assertThat(serviceJava.totalAdditions()).isEqualTo(90),
                () -> assertThat(serviceJava.totalDeletions()).isEqualTo(5),
                () -> assertThat(serviceJava.addedCount()).isEqualTo(1),
                () -> assertThat(serviceJava.renamedCount()).isEqualTo(1)
        );
    }

    @Sql("/sql/webhook/insert_project.sql")
    @Test
    void 파일이_없는_프로젝트의_핫_파일_통계를_조회하면_빈_목록을_반환한다() {
        // given
        Long userId = 1L;
        Long projectId = 1L;
        HotFileStatisticsRequest request = new HotFileStatisticsRequest(null, null, null);

        // when
        HotFileStatisticsResponse actual = hotFileStatisticsQueryService.findHotFileStatistics(userId, projectId, request);

        // then
        assertThat(actual.hotFiles()).isEmpty();
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_hot_file_statistics.sql")
    @Test
    void limit_파라미터로_조회_결과_수를_제한한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        HotFileStatisticsRequest request = new HotFileStatisticsRequest(2, null, null);

        // when
        HotFileStatisticsResponse actual = hotFileStatisticsQueryService.findHotFileStatistics(userId, projectId, request);

        // then
        assertAll(
                () -> assertThat(actual.hotFiles()).hasSize(2),
                () -> assertThat(actual.hotFiles().get(0).fileName()).isEqualTo("src/main/java/Application.java"),
                () -> assertThat(actual.hotFiles().get(0).changeCount()).isEqualTo(3)
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_hot_file_statistics_date_range.sql")
    @Test
    void 시작일과_종료일을_지정하면_해당_범위의_핫_파일_통계만_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 20);
        LocalDate endDate = LocalDate.of(2024, 2, 15);
        HotFileStatisticsRequest request = new HotFileStatisticsRequest(null, startDate, endDate);

        // when
        HotFileStatisticsResponse actual = hotFileStatisticsQueryService.findHotFileStatistics(userId, projectId, request);

        // then
        // PR2(Jan 20) + PR3(Feb 15) 포함, PR1(Jan 10) 및 PR4(Mar 1) 제외
        // Application.java: 2회 (PR2 MODIFIED, PR3 MODIFIED)
        // Config.java: 2회 (PR2 ADDED, PR3 MODIFIED)
        // README.md: 1회 (PR2 MODIFIED)
        assertAll(
                () -> assertThat(actual.hotFiles()).hasSize(3),
                () -> assertThat(actual.hotFiles())
                        .extracting(HotFileStatistics::fileName)
                        .containsExactlyInAnyOrder(
                                "src/main/java/Application.java",
                                "src/main/java/Config.java",
                                "README.md"
                        ),
                () -> {
                    HotFileStatistics appJava = actual.hotFiles().stream()
                            .filter(f -> f.fileName().equals("src/main/java/Application.java"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(appJava.changeCount()).isEqualTo(2);
                    assertThat(appJava.totalAdditions()).isEqualTo(160);
                    assertThat(appJava.totalDeletions()).isEqualTo(65);
                },
                () -> {
                    HotFileStatistics config = actual.hotFiles().stream()
                            .filter(f -> f.fileName().equals("src/main/java/Config.java"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(config.changeCount()).isEqualTo(2);
                    assertThat(config.totalAdditions()).isEqualTo(150);
                    assertThat(config.totalDeletions()).isEqualTo(30);
                }
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_hot_file_statistics_date_range.sql")
    @Test
    void 시작일만_지정하면_시작일_이후의_핫_파일_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate startDate = LocalDate.of(2024, 2, 15);
        HotFileStatisticsRequest request = new HotFileStatisticsRequest(null, startDate, null);

        // when
        HotFileStatisticsResponse actual = hotFileStatisticsQueryService.findHotFileStatistics(userId, projectId, request);

        // then
        // PR3(Feb 15) + PR4(Mar 1) 포함, PR1(Jan 10) 및 PR2(Jan 20) 제외
        // Application.java: 2회 (PR3 MODIFIED, PR4 MODIFIED)
        // Config.java: 1회 (PR3 MODIFIED)
        // Service.java: 1회 (PR4 ADDED)
        assertAll(
                () -> assertThat(actual.hotFiles()).hasSize(3),
                () -> assertThat(actual.hotFiles())
                        .extracting(HotFileStatistics::fileName)
                        .containsExactlyInAnyOrder(
                                "src/main/java/Application.java",
                                "src/main/java/Config.java",
                                "src/main/java/Service.java"
                        ),
                () -> {
                    HotFileStatistics appJava = actual.hotFiles().stream()
                            .filter(f -> f.fileName().equals("src/main/java/Application.java"))
                            .findFirst()
                            .orElseThrow();
                    assertThat(appJava.changeCount()).isEqualTo(2);
                    assertThat(appJava.totalAdditions()).isEqualTo(140);
                    assertThat(appJava.totalDeletions()).isEqualTo(60);
                }
        );
    }

    @Sql("/sql/statistics/insert_project_and_pull_requests_for_hot_file_statistics_date_range.sql")
    @Test
    void 종료일만_지정하면_종료일_이전의_핫_파일_통계를_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        LocalDate endDate = LocalDate.of(2024, 1, 20);
        HotFileStatisticsRequest request = new HotFileStatisticsRequest(null, null, endDate);

        // when
        HotFileStatisticsResponse actual = hotFileStatisticsQueryService.findHotFileStatistics(userId, projectId, request);

        // then
        // PR1(Jan 10) + PR2(Jan 20) 포함, PR3(Feb 15) 및 PR4(Mar 1) 제외
        // Application.java: 2회 (PR1 MODIFIED, PR2 MODIFIED)
        // README.md: 2회 (PR1 ADDED, PR2 MODIFIED)
        // Config.java: 1회 (PR2 ADDED)
        assertAll(
                () -> assertThat(actual.hotFiles()).hasSize(3),
                () -> assertThat(actual.hotFiles())
                        .extracting(HotFileStatistics::fileName)
                        .containsExactlyInAnyOrder(
                                "src/main/java/Application.java",
                                "README.md",
                                "src/main/java/Config.java"
                        )
        );
    }

    @Test
    void 소유하지_않은_프로젝트의_핫_파일_통계를_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;
        HotFileStatisticsRequest request = new HotFileStatisticsRequest(null, null, null);

        // when & then
        assertThatThrownBy(() -> hotFileStatisticsQueryService.findHotFileStatistics(userId, projectId, request))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }
}
