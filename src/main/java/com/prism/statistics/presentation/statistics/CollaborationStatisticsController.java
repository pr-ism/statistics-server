package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.CollaborationStatisticsQueryService;
import com.prism.statistics.application.statistics.dto.request.CollaborationStatisticsRequest;
import com.prism.statistics.application.statistics.dto.response.CollaborationStatisticsResponse;
import com.prism.statistics.global.auth.AuthUserId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/statistics")
@RequiredArgsConstructor
public class CollaborationStatisticsController {

    private final CollaborationStatisticsQueryService collaborationStatisticsQueryService;

    @GetMapping("/collaboration")
    public ResponseEntity<CollaborationStatisticsResponse> getCollaborationStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute CollaborationStatisticsRequest request
    ) {
        CollaborationStatisticsResponse response = collaborationStatisticsQueryService.findCollaborationStatistics(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
