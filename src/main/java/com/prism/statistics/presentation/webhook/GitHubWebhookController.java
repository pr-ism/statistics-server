package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.PrOpenedHandler;
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

    private final PrOpenedHandler prOpenedHandler;

    @PostMapping("/pr/opened")
    public ResponseEntity<Void> handlePrOpened(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PrOpenedRequest request
    ) {
        prOpenedHandler.handle(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
