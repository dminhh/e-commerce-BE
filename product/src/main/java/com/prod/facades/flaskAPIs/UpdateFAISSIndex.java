package com.prod.facades.flaskAPIs;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class UpdateFAISSIndex {
    private final String FLASK_URL = "http://localhost:5000/update_faiss_index"; // URL endpoint Flask

    public String updateIndex(int id, String title) {
        try {
            // Bước 1: Tạo RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Bước 2: Chuẩn bị dữ liệu JSON
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("id", id);
            requestData.put("title", title);

            // Bước 3: Tạo HttpHeaders và HttpEntity
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestData, headers);

            // Bước 4: Gửi yêu cầu POST đến Flask
            ResponseEntity<String> response = restTemplate.exchange(
                    FLASK_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Bước 5: Trả về phản hồi từ Flask
            return response.getBody();

        } catch (Exception e) {
            return "Đã xảy ra lỗi khi cập nhật FAISS index: " + e.getMessage();
        }
    }
}
