package com.prism.statistics.application.project;

import com.prism.statistics.application.project.dto.request.UpdateCoreTimeRequest;
import com.prism.statistics.application.project.dto.request.UpdateSizeGradeThresholdRequest;
import com.prism.statistics.application.project.dto.request.UpdateSizeWeightRequest;
import com.prism.statistics.application.project.dto.response.CoreTimeResponse;
import com.prism.statistics.application.project.dto.response.SizeGradeThresholdResponse;
import com.prism.statistics.application.project.dto.response.SizeWeightResponse;
import com.prism.statistics.domain.analysis.insight.size.vo.SizeScoreWeight;
import com.prism.statistics.domain.project.exception.ProjectOwnershipException;
import com.prism.statistics.domain.project.exception.ProjectSettingNotFoundException;
import com.prism.statistics.domain.project.repository.ProjectRepository;
import com.prism.statistics.domain.project.setting.ProjectCoreTimeSetting;
import com.prism.statistics.domain.project.setting.ProjectSizeGradeThresholdSetting;
import com.prism.statistics.domain.project.setting.ProjectSizeWeightSetting;
import com.prism.statistics.domain.project.setting.repository.ProjectCoreTimeSettingRepository;
import com.prism.statistics.domain.project.setting.repository.ProjectSizeGradeThresholdSettingRepository;
import com.prism.statistics.domain.project.setting.repository.ProjectSizeWeightSettingRepository;
import com.prism.statistics.domain.project.setting.vo.CoreTime;
import com.prism.statistics.domain.project.setting.vo.SizeGradeThreshold;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectSettingService {

    private final ProjectRepository projectRepository;
    private final ProjectCoreTimeSettingRepository projectCoreTimeSettingRepository;
    private final ProjectSizeWeightSettingRepository projectSizeWeightSettingRepository;
    private final ProjectSizeGradeThresholdSettingRepository projectSizeGradeThresholdSettingRepository;

    @Transactional(readOnly = true)
    public CoreTimeResponse findCoreTime(Long userId, Long projectId) {
        validateProjectOwnership(projectId, userId);

        ProjectCoreTimeSetting setting = projectCoreTimeSettingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectSettingNotFoundException("코어타임 설정이 존재하지 않습니다."));

        return CoreTimeResponse.from(setting);
    }

    @Transactional
    public CoreTimeResponse updateCoreTime(Long userId, Long projectId, UpdateCoreTimeRequest request) {
        validateProjectOwnership(projectId, userId);

        ProjectCoreTimeSetting setting = projectCoreTimeSettingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectSettingNotFoundException("코어타임 설정이 존재하지 않습니다."));

        setting.changeCoreTime(CoreTime.of(request.startTime(), request.endTime()));

        return CoreTimeResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public SizeWeightResponse findSizeWeight(Long userId, Long projectId) {
        validateProjectOwnership(projectId, userId);

        ProjectSizeWeightSetting setting = projectSizeWeightSettingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectSettingNotFoundException("사이즈 가중치 설정이 존재하지 않습니다."));

        return SizeWeightResponse.from(setting);
    }

    @Transactional
    public SizeWeightResponse updateSizeWeight(Long userId, Long projectId, UpdateSizeWeightRequest request) {
        validateProjectOwnership(projectId, userId);

        ProjectSizeWeightSetting setting = projectSizeWeightSettingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectSettingNotFoundException("사이즈 가중치 설정이 존재하지 않습니다."));

        setting.changeWeight(
                SizeScoreWeight.of(request.additionWeight(), request.deletionWeight(), request.fileWeight()));

        return SizeWeightResponse.from(setting);
    }

    @Transactional(readOnly = true)
    public SizeGradeThresholdResponse findSizeGradeThreshold(Long userId, Long projectId) {
        validateProjectOwnership(projectId, userId);

        ProjectSizeGradeThresholdSetting setting = projectSizeGradeThresholdSettingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectSettingNotFoundException("사이즈 등급 임계값 설정이 존재하지 않습니다."));

        return SizeGradeThresholdResponse.from(setting);
    }

    @Transactional
    public SizeGradeThresholdResponse updateSizeGradeThreshold(Long userId, Long projectId,
                                                               UpdateSizeGradeThresholdRequest request) {
        validateProjectOwnership(projectId, userId);

        ProjectSizeGradeThresholdSetting setting = projectSizeGradeThresholdSettingRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ProjectSettingNotFoundException("사이즈 등급 임계값 설정이 존재하지 않습니다."));

        setting.changeThreshold(SizeGradeThreshold.of(
                request.sThreshold(), request.mThreshold(), request.lThreshold(), request.xlThreshold()
        ));

        return SizeGradeThresholdResponse.from(setting);
    }

    private void validateProjectOwnership(Long projectId, Long userId) {
        if (!projectRepository.existsByIdAndUserId(projectId, userId)) {
            throw new ProjectOwnershipException();
        }
    }
}
