package com.prism.statistics.application.project;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.prism.statistics.application.IntegrationTest;
import com.prism.statistics.application.project.dto.request.UpdateCoreTimeRequest;
import com.prism.statistics.application.project.dto.request.UpdateSizeGradeThresholdRequest;
import com.prism.statistics.application.project.dto.request.UpdateSizeWeightRequest;
import com.prism.statistics.application.project.dto.response.CoreTimeResponse;
import com.prism.statistics.application.project.dto.response.SizeGradeThresholdResponse;
import com.prism.statistics.application.project.dto.response.SizeWeightResponse;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import java.math.BigDecimal;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectSettingServiceTest {

    @Autowired
    private ProjectSettingService projectSettingService;

    @Sql("/sql/project/insert_project_settings.sql")
    @Test
    void 코어타임_설정을_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;

        // when
        CoreTimeResponse actual = projectSettingService.findCoreTime(userId, projectId);

        // then
        assertAll(
                () -> assertThat(actual.startTime()).isEqualTo(LocalTime.of(10, 0)),
                () -> assertThat(actual.endTime()).isEqualTo(LocalTime.of(18, 0))
        );
    }

    @Sql("/sql/project/insert_project_settings.sql")
    @Test
    void 코어타임_설정을_수정한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        UpdateCoreTimeRequest request = new UpdateCoreTimeRequest(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        // when
        CoreTimeResponse actual = projectSettingService.updateCoreTime(userId, projectId, request);

        // then
        assertAll(
                () -> assertThat(actual.startTime()).isEqualTo(LocalTime.of(9, 0)),
                () -> assertThat(actual.endTime()).isEqualTo(LocalTime.of(17, 0))
        );
    }

    @Sql("/sql/project/insert_project_settings.sql")
    @Test
    void 사이즈_가중치_설정을_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;

        // when
        SizeWeightResponse actual = projectSettingService.findSizeWeight(userId, projectId);

        // then
        assertAll(
                () -> assertThat(actual.additionWeight()).isEqualByComparingTo(BigDecimal.ONE),
                () -> assertThat(actual.deletionWeight()).isEqualByComparingTo(BigDecimal.ONE),
                () -> assertThat(actual.fileWeight()).isEqualByComparingTo(BigDecimal.ONE)
        );
    }

    @Sql("/sql/project/insert_project_settings.sql")
    @Test
    void 사이즈_가중치_설정을_수정한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        UpdateSizeWeightRequest request = new UpdateSizeWeightRequest(
                new BigDecimal("2.0"),
                new BigDecimal("0.5"),
                new BigDecimal("3.0")
        );

        // when
        SizeWeightResponse actual = projectSettingService.updateSizeWeight(userId, projectId, request);

        // then
        assertAll(
                () -> assertThat(actual.additionWeight()).isEqualByComparingTo(new BigDecimal("2.0")),
                () -> assertThat(actual.deletionWeight()).isEqualByComparingTo(new BigDecimal("0.5")),
                () -> assertThat(actual.fileWeight()).isEqualByComparingTo(new BigDecimal("3.0"))
        );
    }

    @Test
    void 소유하지_않은_프로젝트의_코어타임을_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;

        // when & then
        assertThatThrownBy(() -> projectSettingService.findCoreTime(userId, projectId))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }

    @Test
    void 소유하지_않은_프로젝트의_사이즈_가중치를_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;

        // when & then
        assertThatThrownBy(() -> projectSettingService.findSizeWeight(userId, projectId))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }

    @Sql("/sql/project/insert_project_settings.sql")
    @Test
    void 사이즈_등급_임계값_설정을_조회한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;

        // when
        SizeGradeThresholdResponse actual = projectSettingService.findSizeGradeThreshold(userId, projectId);

        // then
        assertAll(
                () -> assertThat(actual.sThreshold()).isEqualTo(10),
                () -> assertThat(actual.mThreshold()).isEqualTo(100),
                () -> assertThat(actual.lThreshold()).isEqualTo(300),
                () -> assertThat(actual.xlThreshold()).isEqualTo(1000)
        );
    }

    @Sql("/sql/project/insert_project_settings.sql")
    @Test
    void 사이즈_등급_임계값_설정을_수정한다() {
        // given
        Long userId = 7L;
        Long projectId = 1L;
        UpdateSizeGradeThresholdRequest request = new UpdateSizeGradeThresholdRequest(20, 200, 500, 2000);

        // when
        SizeGradeThresholdResponse actual = projectSettingService.updateSizeGradeThreshold(userId, projectId, request);

        // then
        assertAll(
                () -> assertThat(actual.sThreshold()).isEqualTo(20),
                () -> assertThat(actual.mThreshold()).isEqualTo(200),
                () -> assertThat(actual.lThreshold()).isEqualTo(500),
                () -> assertThat(actual.xlThreshold()).isEqualTo(2000)
        );
    }

    @Test
    void 소유하지_않은_프로젝트의_사이즈_등급_임계값을_조회하면_예외가_발생한다() {
        // given
        Long userId = 999L;
        Long projectId = 1L;

        // when & then
        assertThatThrownBy(() -> projectSettingService.findSizeGradeThreshold(userId, projectId))
                .isInstanceOf(ProjectOwnershipException.class)
                .hasMessage("프로젝트를 찾을 수 없습니다.");
    }
}
