package com.prism.statistics.presentation.statistics;

import com.prism.statistics.application.statistics.StatisticsSummaryQueryService;
import com.prism.statistics.application.statistics.dto.request.StatisticsSummaryRequest;
import com.prism.statistics.application.statistics.dto.response.StatisticsSummaryResponse;
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
public class StatisticsSummaryController {

    private final StatisticsSummaryQueryService statisticsSummaryQueryService;

    @GetMapping("/summary")
    public ResponseEntity<StatisticsSummaryResponse> getStatisticsSummary(
            AuthUserId authUserId,
            @PathVariable Long projectId,
            @Valid @ModelAttribute StatisticsSummaryRequest request
    ) {
        StatisticsSummaryResponse response = statisticsSummaryQueryService.findStatisticsSummary(
                authUserId.userId(),
                projectId,
                request
        );

        return ResponseEntity.ok(response);
    }
}
