package com.prism.statistics.presentation.metric;

import com.prism.statistics.application.metric.AuthorStatisticsQueryService;
import com.prism.statistics.application.metric.dto.response.AuthorStatisticsResponse;
import com.prism.statistics.global.auth.AuthUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects/{projectId}/statistics")
@RequiredArgsConstructor
public class AuthorStatisticsController {

    private final AuthorStatisticsQueryService authorStatisticsQueryService;

    @GetMapping("/authors")
    public ResponseEntity<AuthorStatisticsResponse> getAuthorStatistics(
            AuthUserId authUserId,
            @PathVariable Long projectId
    ) {
        AuthorStatisticsResponse response = authorStatisticsQueryService.findAuthorStatistics(
                authUserId.userId(), projectId
        );
        return ResponseEntity.ok(response);
    }
}
