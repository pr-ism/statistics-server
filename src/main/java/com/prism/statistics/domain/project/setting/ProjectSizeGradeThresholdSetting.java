package com.prism.statistics.domain.project.setting;

import com.prism.statistics.domain.common.BaseTimeEntity;
import com.prism.statistics.domain.project.setting.vo.SizeGradeThreshold;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "project_size_grade_threshold_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectSizeGradeThresholdSetting extends BaseTimeEntity {

    private Long projectId;

    @Embedded
    private SizeGradeThreshold threshold;

    public static ProjectSizeGradeThresholdSetting create(Long projectId, SizeGradeThreshold threshold) {
        validateProjectId(projectId);
        validateThreshold(threshold);

        return new ProjectSizeGradeThresholdSetting(projectId, threshold);
    }

    public static ProjectSizeGradeThresholdSetting createDefault(Long projectId) {
        validateProjectId(projectId);

        return new ProjectSizeGradeThresholdSetting(projectId, SizeGradeThreshold.defaultThreshold());
    }

    private static void validateProjectId(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("프로젝트 ID는 필수입니다.");
        }
    }

    private static void validateThreshold(SizeGradeThreshold threshold) {
        if (threshold == null) {
            throw new IllegalArgumentException("사이즈 등급 임계값은 필수입니다.");
        }
    }

    private ProjectSizeGradeThresholdSetting(Long projectId, SizeGradeThreshold threshold) {
        this.projectId = projectId;
        this.threshold = threshold;
    }

    public void changeThreshold(SizeGradeThreshold newThreshold) {
        validateThreshold(newThreshold);
        this.threshold = newThreshold;
    }
}
