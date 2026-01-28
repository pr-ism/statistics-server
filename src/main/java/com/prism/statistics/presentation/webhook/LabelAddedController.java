package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.LabelAddedService;
import com.prism.statistics.application.webhook.dto.request.LabelAddedRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class LabelAddedController {

    private final LabelAddedService labelAddedService;

    @PostMapping("/label/added")
    public ResponseEntity<Void> handleLabelAdded(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody LabelAddedRequest request
    ) {
        labelAddedService.addLabel(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
