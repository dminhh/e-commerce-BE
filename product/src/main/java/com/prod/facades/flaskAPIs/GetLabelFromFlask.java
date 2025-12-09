package com.prod.facades.flaskAPIs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
@Component
@Slf4j
public class GetLabelFromFlask {
    public String[] getLabel(String title) {
        try {
            // Tạo RestTemplate
            RestTemplate restTemplate = new RestTemplate();

            // Tạo request body
            Map<String, String> requestBody = Map.of("title", title);

            // Cấu hình headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

            // Gửi POST request tới Flask
            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://localhost:5000/label",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Xử lý kết quả trả về
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (requestBody.get("title") != null) {
                    assert responseBody != null;
                    List<String> rawLabels = (List<String>)
                            responseBody.get("labels");
                    log.info("Nhan duoc cac nhan: " + rawLabels);
                    // Chuẩn hóa nhãn: loại bỏ khoảng trắng thừa
                    return getLabelFromFlask(rawLabels);
                }
            }
            return null;

        } catch (Exception e) {
            log.error("Lỗi khi gọi Flask API: " + e.getMessage() + "\nVoi san pham " + title);
            return null;
        }
    }
    public String[] getCustomLabel(int productId, String title, List<String> labels) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> requestBody = Map.of(
                    "title", title,
                    "id", productId,
                    "labels", labels
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "http://localhost:5000/generate_custom_labels",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                if (responseBody != null) {
                    List<String> rawLabels = (List<String>) responseBody.get("labels");
                    log.info("Nhan duoc cac nhan: " + rawLabels);
                    return getLabelFromFlask(rawLabels);
                }
            }
            return null;

        } catch (Exception e) {
            log.error("Lỗi khi gọi Flask API: " + e.getMessage());
            return null;
        }
    }


    private String[] getLabelFromFlask(List<String> rawLabels) {
        return rawLabels.stream()
                .map(String::trim)
                .filter(label -> !label.isEmpty()) // Loại bỏ nhãn rỗng
                .toArray(String[]::new);
    }
}
