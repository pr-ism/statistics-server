package com.prism.statistics.domain.project.setting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.prism.statistics.domain.project.setting.vo.CoreTime;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@SuppressWarnings("NonAsciiCharacters")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ProjectCoreTimeSettingTest {

    @Test
    void 프로젝트별_코어타임_설정을_생성한다() {
        // given
        Long projectId = 1L;
        CoreTime coreTime = CoreTime.of(LocalTime.of(9, 0), LocalTime.of(18, 0));

        // when
        ProjectCoreTimeSetting setting = assertDoesNotThrow(
                () -> ProjectCoreTimeSetting.create(projectId, coreTime)
        );

        // then
        assertAll(
                () -> assertThat(setting.getProjectId()).isEqualTo(projectId),
                () -> assertThat(setting.getCoreTime()).isEqualTo(coreTime)
        );
    }

    @Test
    void 기본_코어타임으로_설정을_생성한다() {
        // given
        Long projectId = 1L;

        // when
        ProjectCoreTimeSetting setting = ProjectCoreTimeSetting.createDefault(projectId);

        // then
        assertAll(
                () -> assertThat(setting.getProjectId()).isEqualTo(projectId),
                () -> assertThat(setting.getCoreTime()).isEqualTo(CoreTime.defaultCoreTime())
        );
    }

    @Test
    void 프로젝트_ID가_null이면_예외가_발생한다() {
        // given
        CoreTime coreTime = CoreTime.defaultCoreTime();

        // when & then
        assertThatThrownBy(() -> ProjectCoreTimeSetting.create(null, coreTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트 ID는 필수입니다.");
    }

    @Test
    void 기본_코어타임_생성_시_프로젝트_ID가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ProjectCoreTimeSetting.createDefault(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("프로젝트 ID는 필수입니다.");
    }

    @Test
    void 코어타임이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> ProjectCoreTimeSetting.create(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("코어타임은 필수입니다.");
    }

    @Test
    void 코어타임을_변경한다() {
        // given
        ProjectCoreTimeSetting setting = ProjectCoreTimeSetting.createDefault(1L);
        CoreTime newCoreTime = CoreTime.of(LocalTime.of(9, 0), LocalTime.of(17, 0));

        // when
        setting.changeCoreTime(newCoreTime);

        // then
        assertThat(setting.getCoreTime()).isEqualTo(newCoreTime);
    }

    @Test
    void 변경할_코어타임이_null이면_예외가_발생한다() {
        // given
        ProjectCoreTimeSetting setting = ProjectCoreTimeSetting.createDefault(1L);

        // when & then
        assertThatThrownBy(() -> setting.changeCoreTime(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("코어타임은 필수입니다.");
    }
}
