package com.prod.facades.flaskAPIs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class UpdateFAISSIndex {
    private final String FLASK_URL = "http://localhost:5001/update-product-index"; // URL endpoint Flask
    private final RestTemplate restTemplate;

    public UpdateFAISSIndex() {
        // Cấu hình RestTemplate với timeout
        this.restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Async
    public CompletableFuture<String> updateIndex() {
        try {
            log.info("Bắt đầu cập nhật FAISS index (async)...");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    FLASK_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Cập nhật FAISS index thành công");
            return CompletableFuture.completedFuture(response.getBody());

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật FAISS index: {}", e.getMessage());
            return CompletableFuture.completedFuture("Đã xảy ra lỗi khi cập nhật product index: " + e.getMessage());
        }
    }
}
