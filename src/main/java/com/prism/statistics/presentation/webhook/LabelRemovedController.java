package com.prism.statistics.presentation.webhook;

import com.prism.statistics.application.webhook.LabelRemovedService;
import com.prism.statistics.application.webhook.dto.request.LabelRemovedRequest;
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
public class LabelRemovedController {

    private final LabelRemovedService labelRemovedService;

    @PostMapping("/label/removed")
    public ResponseEntity<Void> handleLabelRemoved(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody LabelRemovedRequest request
    ) {
        labelRemovedService.removeLabel(apiKey, request);
        return ResponseEntity.ok().build();
    }
}
