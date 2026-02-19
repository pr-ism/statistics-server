package com.prism.statistics.domain.project.setting;

import com.prism.statistics.domain.common.BaseTimeEntity;
import com.prism.statistics.domain.project.setting.vo.CoreTime;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "project_core_time_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectCoreTimeSetting extends BaseTimeEntity {

    private Long projectId;

    @Embedded
    private CoreTime coreTime;

    public static ProjectCoreTimeSetting create(Long projectId, CoreTime coreTime) {
        validateProjectId(projectId);
        validateCoreTime(coreTime);

        return new ProjectCoreTimeSetting(projectId, coreTime);
    }

    public static ProjectCoreTimeSetting createDefault(Long projectId) {
        validateProjectId(projectId);

        return new ProjectCoreTimeSetting(projectId, CoreTime.defaultCoreTime());
    }

    private static void validateProjectId(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("프로젝트 ID는 필수입니다.");
        }
    }

    private static void validateCoreTime(CoreTime coreTime) {
        if (coreTime == null) {
            throw new IllegalArgumentException("코어타임은 필수입니다.");
        }
    }

    private ProjectCoreTimeSetting(Long projectId, CoreTime coreTime) {
        this.projectId = projectId;
        this.coreTime = coreTime;
    }

    public void changeCoreTime(CoreTime newCoreTime) {
        validateCoreTime(newCoreTime);
        this.coreTime = newCoreTime;
    }
}
