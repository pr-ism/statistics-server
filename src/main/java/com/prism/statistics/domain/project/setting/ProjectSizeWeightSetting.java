package com.prism.statistics.domain.project.setting;

import com.prism.statistics.domain.analysis.insight.size.vo.SizeScoreWeight;
import com.prism.statistics.domain.common.BaseTimeEntity;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "project_size_weight_settings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectSizeWeightSetting extends BaseTimeEntity {

    private Long projectId;

    @Embedded
    private SizeScoreWeight weight;

    public static ProjectSizeWeightSetting create(Long projectId, SizeScoreWeight weight) {
        validateProjectId(projectId);
        validateWeight(weight);

        return new ProjectSizeWeightSetting(projectId, weight);
    }

    public static ProjectSizeWeightSetting createDefault(Long projectId) {
        validateProjectId(projectId);

        return new ProjectSizeWeightSetting(projectId, SizeScoreWeight.defaultWeight());
    }

    private static void validateProjectId(Long projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("프로젝트 ID는 필수입니다.");
        }
    }

    private static void validateWeight(SizeScoreWeight weight) {
        if (weight == null) {
            throw new IllegalArgumentException("가중치는 필수입니다.");
        }
    }

    private ProjectSizeWeightSetting(Long projectId, SizeScoreWeight weight) {
        this.projectId = projectId;
        this.weight = weight;
    }

    public void changeWeight(SizeScoreWeight newWeight) {
        validateWeight(newWeight);
        this.weight = newWeight;
    }
}
