package com.prism.statistics.domain.project.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.prism.statistics.domain.analysis.insight.size.vo.SizeScoreWeight;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectSizeWeightSettingTest {

    @Test
    void 프로젝트별_가중치_설정을_생성한다() {
        // given
        Long projectId = 1L;
        SizeScoreWeight weight = SizeScoreWeight.of(
                new BigDecimal("1.5"),
                new BigDecimal("0.5"),
                new BigDecimal("10.0")
        );

        // when
        ProjectSizeWeightSetting setting = assertDoesNotThrow(
                () -> ProjectSizeWeightSetting.create(projectId, weight)
        );

        // then
        assertAll(
                () -> assertThat(setting.getProjectId()).isEqualTo(projectId),
                () -> assertThat(setting.getWeight()).isEqualTo(weight)
        );
    }

    @Test
    void 기본_가중치로_설정을_생성한다() {
        // given
        Long projectId = 1L;

        // when
        ProjectSizeWeightSetting setting = ProjectSizeWeightSetting.createDefault(projectId);

        // then
        assertAll(
                () -> assertThat(setting.getProjectId()).isEqualTo(projectId),
                () -> assertThat(setting.getWeight()).isEqualTo(SizeScoreWeight.defaultWeight())
        );
    }

    @Test
    void 프로젝트_ID가_null이면_예외가_발생한다() {
        // given
        SizeScoreWeight weight = SizeScoreWeight.defaultWeight();

        // when & then
        assertThatThrownBy(() -> ProjectSizeWeightSetting.create(null, weight))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트 ID는 필수입니다.");
    }

    @Test
    void 기본_가중치_생성_시_프로젝트_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ProjectSizeWeightSetting.createDefault(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트 ID는 필수입니다.");
    }

    @Test
    void 가중치가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ProjectSizeWeightSetting.create(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가중치는 필수입니다.");
    }

    @Test
    void 가중치를_변경한다() {
        // given
        ProjectSizeWeightSetting setting = ProjectSizeWeightSetting.createDefault(1L);
        SizeScoreWeight newWeight = SizeScoreWeight.of(
                new BigDecimal("2.0"),
                new BigDecimal("1.5"),
                new BigDecimal("5.0")
        );

        // when
        setting.changeWeight(newWeight);

        // then
        assertThat(setting.getWeight()).isEqualTo(newWeight);
    }

    @Test
    void 변경할_가중치가_null이면_예외가_발생한다() {
        // given
        ProjectSizeWeightSetting setting = ProjectSizeWeightSetting.createDefault(1L);

        // when & then
        assertThatThrownBy(() -> setting.changeWeight(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("가중치는 필수입니다.");
    }
}
