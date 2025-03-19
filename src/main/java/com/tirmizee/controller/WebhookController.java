package com.tirmizee.controller;

import com.tirmizee.model.WebhookRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class WebhookController {

    @PostMapping("/webhook")
    public Mono<ResponseEntity<String>> handleWebhook(@RequestHeader("X-Line-Signature") String signature,
                                                      @RequestBody WebhookRequest request) {
        log.info("Received LINE Signature: {}", signature);
        log.info("Received LINE Request: {}", request);

        return Mono.just(ResponseEntity.ok("Webhook received"));
    }
}
