package com.tirmizee.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
@Order(1)
@Component
@RequiredArgsConstructor
public class LineSignatureWebFilter implements WebFilter {

    private static final String LINE_SIGNATURE_HEADER = "X-Line-Signature";
    private static final String WEBHOOK_PATH = "/webhook";

    @Value("${line.bot.channel-secret}")
    private String channelSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (!path.equals(WEBHOOK_PATH)) {
            return chain.filter(exchange);
        }

        String signature = request.getHeaders().getFirst(LINE_SIGNATURE_HEADER);
        if (signature == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return cacheRequestBody(exchange, signature, chain);
    }

    private Mono<Void> cacheRequestBody(ServerWebExchange exchange, String signature, WebFilterChain chain) {
        return DataBufferUtils.join(exchange.getRequest().getBody())
                .flatMap(dataBuffer -> {

                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);

                    String payload = new String(bytes, UTF_8);
                    log.info("Raw payload: {}", payload);

                    if (!isValidSignature(payload, signature)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    }

                    // Create a new cached request with the same body
                    ServerHttpRequest mutatedRequest = new ServerHttpRequestDecorator(exchange.getRequest()) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return Flux.just(exchange.getResponse().bufferFactory().wrap(bytes));
                        }
                    };

                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                });
    }

    private boolean isValidSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(channelSecret.getBytes(UTF_8), "HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(payload.getBytes(UTF_8));
            String calculatedSignature = Base64.encodeBase64String(hash);

            return calculatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature validation failed", e);
            return false;
        }
    }
}
