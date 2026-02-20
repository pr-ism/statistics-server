package com.prism.statistics.presentation.project;

import com.prism.statistics.application.project.ProjectSettingService;
import com.prism.statistics.application.project.dto.request.UpdateCoreTimeRequest;
import com.prism.statistics.application.project.dto.request.UpdateSizeGradeThresholdRequest;
import com.prism.statistics.application.project.dto.request.UpdateSizeWeightRequest;
import com.prism.statistics.application.project.dto.response.CoreTimeResponse;
import com.prism.statistics.application.project.dto.response.SizeGradeThresholdResponse;
import com.prism.statistics.application.project.dto.response.SizeWeightResponse;
import com.prism.statistics.global.auth.AuthUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/settings")
@RequiredArgsConstructor
public class ProjectSettingController {

    private final ProjectSettingService projectSettingService;

    @GetMapping("/core-time")
    public ResponseEntity<CoreTimeResponse> getCoreTime(
            @PathVariable Long projectId,
            AuthUserId authUserId
    ) {
        CoreTimeResponse response = projectSettingService.findCoreTime(authUserId.userId(), projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/core-time")
    public ResponseEntity<CoreTimeResponse> updateCoreTime(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateCoreTimeRequest request,
            AuthUserId authUserId
    ) {
        CoreTimeResponse response = projectSettingService.updateCoreTime(authUserId.userId(), projectId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/size-weight")
    public ResponseEntity<SizeWeightResponse> getSizeWeight(
            @PathVariable Long projectId,
            AuthUserId authUserId
    ) {
        SizeWeightResponse response = projectSettingService.findSizeWeight(authUserId.userId(), projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/size-weight")
    public ResponseEntity<SizeWeightResponse> updateSizeWeight(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateSizeWeightRequest request,
            AuthUserId authUserId
    ) {
        SizeWeightResponse response = projectSettingService.updateSizeWeight(authUserId.userId(), projectId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/size-grade-threshold")
    public ResponseEntity<SizeGradeThresholdResponse> getSizeGradeThreshold(
            @PathVariable Long projectId,
            AuthUserId authUserId
    ) {
        SizeGradeThresholdResponse response = projectSettingService.findSizeGradeThreshold(authUserId.userId(), projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/size-grade-threshold")
    public ResponseEntity<SizeGradeThresholdResponse> updateSizeGradeThreshold(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateSizeGradeThresholdRequest request,
            AuthUserId authUserId
    ) {
        SizeGradeThresholdResponse response = projectSettingService.updateSizeGradeThreshold(authUserId.userId(), projectId, request);
        return ResponseEntity.ok(response);
    }
}
