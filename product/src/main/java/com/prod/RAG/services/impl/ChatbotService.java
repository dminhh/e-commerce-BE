package com.prod.RAG.services.impl;

import com.prod.RAG.model.ChatRequest;
import com.prod.RAG.model.ChatResponse;
import com.prod.RAG.model.FlaskChatResponse;
import com.prod.RAG.services.IChatbotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChatbotService implements IChatbotService {

    private final RestTemplate restTemplate;

    @Value("${chatbot.flask.url:http://localhost:5001}")
    private String flaskUrl;

    @Value("${chatbot.flask.chat-endpoint:/chat}")
    private String chatEndpoint;

    @Value("${chatbot.flask.timeout-ms:10000}")
    private Integer timeoutMs;

    public ChatbotService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public ChatResponse chat(ChatRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Gửi câu hỏi tới Flask chatbot: {}", request.getQuestion());

            // Tạo request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("question", request.getQuestion());
            requestBody.put("debug", request.getDebug() != null ? request.getDebug() : false);

            // Cấu hình headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Gửi POST request tới Flask
            String url = flaskUrl + chatEndpoint;
            ResponseEntity<FlaskChatResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    FlaskChatResponse.class
            );

            long responseTime = System.currentTimeMillis() - startTime;

            // Xử lý response
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                FlaskChatResponse flaskResponse = response.getBody();

                if (flaskResponse.getSuccess() && flaskResponse.getData() != null) {
                    log.info("Nhận được câu trả lời từ chatbot trong {}ms", responseTime);
                    return mapFlaskResponseToChatResponse(flaskResponse);
                } else {
                    log.error("Flask chatbot trả về lỗi: {}", flaskResponse.getError());
                    return ChatResponse.builder()
                            .success(false)
                            .error(flaskResponse.getError() != null ?
                                   flaskResponse.getError() : "Lỗi không xác định từ chatbot")
                            .build();
                }
            } else {
                log.error("Không nhận được response từ Flask chatbot");
                return ChatResponse.builder()
                        .success(false)
                        .error("Không nhận được phản hồi từ chatbot")
                        .build();
            }

        } catch (HttpClientErrorException e) {
            log.error("Lỗi client (4xx) khi gọi Flask chatbot: {} - {}",
                     e.getStatusCode(), e.getResponseBodyAsString());
            return ChatResponse.builder()
                    .success(false)
                    .error("Yêu cầu không hợp lệ: " + e.getMessage())
                    .build();

        } catch (HttpServerErrorException e) {
            log.error("Lỗi server (5xx) từ Flask chatbot: {} - {}",
                     e.getStatusCode(), e.getResponseBodyAsString());
            return ChatResponse.builder()
                    .success(false)
                    .error("Chatbot service đang gặp sự cố, vui lòng thử lại sau")
                    .build();

        } catch (ResourceAccessException e) {
            log.error("Không thể kết nối tới Flask chatbot: {}", e.getMessage());
            return ChatResponse.builder()
                    .success(false)
                    .error("Không thể kết nối tới chatbot service. Vui lòng kiểm tra Flask service đang chạy trên " + flaskUrl)
                    .build();

        } catch (Exception e) {
            log.error("Lỗi không xác định khi gọi Flask chatbot: ", e);
            return ChatResponse.builder()
                    .success(false)
                    .error("Đã xảy ra lỗi: " + e.getMessage())
                    .build();
        }
    }


    private ChatResponse mapFlaskResponseToChatResponse(FlaskChatResponse flaskResponse) {
        FlaskChatResponse.FlaskChatData flaskData = flaskResponse.getData();

        // Map search results
        List<ChatResponse.SearchResult> searchResults = null;
        if (flaskData.getSearchResults() != null) {
            searchResults = flaskData.getSearchResults().stream()
                    .map(sr -> ChatResponse.SearchResult.builder()
                            .content(sr.getContent())
                            .score(sr.getScore())
                            .index(sr.getIndex())
                            .build())
                    .collect(Collectors.toList());
        }

        ChatResponse.ChatData chatData = ChatResponse.ChatData.builder()
                .question(flaskData.getQuestion())
                .answer(flaskData.getAnswer())
                .isConfident(flaskData.getIsConfident())
                .searchResults(searchResults)
                .build();

        return ChatResponse.builder()
                .success(true)
                .data(chatData)
                .build();
    }
}
