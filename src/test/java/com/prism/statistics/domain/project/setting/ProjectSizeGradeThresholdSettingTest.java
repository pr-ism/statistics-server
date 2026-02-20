package com.prism.statistics.domain.project.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.prism.statistics.domain.project.setting.vo.SizeGradeThreshold;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectSizeGradeThresholdSettingTest {

    @Test
    void 프로젝트별_사이즈_등급_임계값_설정을_생성한다() {
        // given
        Long projectId = 1L;
        SizeGradeThreshold threshold = SizeGradeThreshold.of(20, 200, 500, 2000);

        // when
        ProjectSizeGradeThresholdSetting setting = assertDoesNotThrow(
                () -> ProjectSizeGradeThresholdSetting.create(projectId, threshold)
        );

        // then
        assertAll(
                () -> assertThat(setting.getProjectId()).isEqualTo(projectId),
                () -> assertThat(setting.getThreshold()).isEqualTo(threshold)
        );
    }

    @Test
    void 기본_임계값으로_설정을_생성한다() {
        // given
        Long projectId = 1L;

        // when
        ProjectSizeGradeThresholdSetting setting = ProjectSizeGradeThresholdSetting.createDefault(projectId);

        // then
        assertAll(
                () -> assertThat(setting.getProjectId()).isEqualTo(projectId),
                () -> assertThat(setting.getThreshold()).isEqualTo(SizeGradeThreshold.defaultThreshold())
        );
    }

    @Test
    void 프로젝트_ID가_null이면_예외가_발생한다() {
        // given
        SizeGradeThreshold threshold = SizeGradeThreshold.defaultThreshold();

        // when & then
        assertThatThrownBy(() -> ProjectSizeGradeThresholdSetting.create(null, threshold))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트 ID는 필수입니다.");
    }

    @Test
    void 기본_임계값_생성_시_프로젝트_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ProjectSizeGradeThresholdSetting.createDefault(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트 ID는 필수입니다.");
    }

    @Test
    void 임계값이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ProjectSizeGradeThresholdSetting.create(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사이즈 등급 임계값은 필수입니다.");
    }

    @Test
    void 임계값을_변경한다() {
        // given
        ProjectSizeGradeThresholdSetting setting = ProjectSizeGradeThresholdSetting.createDefault(1L);
        SizeGradeThreshold newThreshold = SizeGradeThreshold.of(20, 200, 500, 2000);

        // when
        setting.changeThreshold(newThreshold);

        // then
        assertThat(setting.getThreshold()).isEqualTo(newThreshold);
    }

    @Test
    void 변경할_임계값이_null이면_예외가_발생한다() {
        // given
        ProjectSizeGradeThresholdSetting setting = ProjectSizeGradeThresholdSetting.createDefault(1L);

        // when & then
        assertThatThrownBy(() -> setting.changeThreshold(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사이즈 등급 임계값은 필수입니다.");
    }
}
