package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.PrOpenedService;
import com.prism.statistics.application.webhook.dto.request.PrOpenedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class GitHubWebhookController {

    private final PrOpenedService prOpenedService;

    @PostMapping("/pr/opened")
    public ResponseEntity<Void> handlePrOpened(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PrOpenedRequest request
    ) {
        prOpenedService.handle(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
