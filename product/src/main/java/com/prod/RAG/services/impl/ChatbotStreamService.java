package com.prod.RAG.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.prod.RAG.model.ChatStreamChunk;
import com.prod.RAG.services.IChatbotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class ChatbotStreamService implements IChatbotService {

    @Autowired
    private WebClient webClient;

    @Value("${chatbot.flask.chat-endpoint:/chat}")
    private String chatEndpoint;

    private final ObjectMapper objectMapper = new ObjectMapper();

//    @Override
//    public Flux<ChatStreamChunk> chatStream(String question) {
//        log.info("Streaming câu hỏi tới Flask chatbot: {}", question);
//
//        // Tạo request body
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("question", question);
//
//        return webClient.post()
//                .uri(chatEndpoint)
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .accept(MediaType.TEXT_EVENT_STREAM)
//                .retrieve()
//                .bodyToFlux(String.class)
//                .map(this::parseSSEData)
//                .filter(chunk -> chunk != null)
//                .doOnError(error -> log.error("Lỗi khi stream từ Flask: ", error))
//                .onErrorResume(error -> {
//                    log.error("Error streaming from Flask: {}", error.getMessage());
//                    return Flux.just(ChatStreamChunk.builder()
//                            .error("Không thể kết nối tới chatbot service: " + error.getMessage())
//                            .done(true)
//                            .build());
//                });
//    }



//    private ChatStreamChunk parseSSEData(String line) {
//        try {
//            // SSE format: "data: {...}\n\n"
//            if (line.startsWith("data: ")) {
//                String jsonData = line.substring(6).trim();
//                return objectMapper.readValue(jsonData, ChatStreamChunk.class);
//            }
//            return null;
//        } catch (Exception e) {
//            log.error("Lỗi parse SSE chunk: {}", line, e);
//            return null;
//        }
//    }

    @Override
    public Flux<ChatStreamChunk> chatStream(String question) {
        log.info("Streaming câu hỏi tới Flask chatbot: {}", question);

        return webClient.post()
                .uri(chatEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Collections.singletonMap("question", question))
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .doOnSubscribe(sub -> log.info("=== Đã subscribe tới Flask"))
                .doOnNext(block -> log.info("=== Flask trả về RAW block (length={}): '{}'", block.length(), block.replace("\n", "\\n")))
                .flatMap(this::parseSSEBlock)
                .doOnNext(streamChunk -> log.info("=== SERVICE parse thành chunk: chunk='{}', done={}",
                        streamChunk.getChunk(), streamChunk.getDone()))
                .doOnComplete(() -> log.info("=== SERVICE hoàn tất"))
                .doOnError(error -> log.error("=== SERVICE lỗi stream từ Flask: ", error))
                .onErrorResume(error -> {
                    log.error("=== SERVICE error resume: {}", error.getMessage());
                    return Flux.just(ChatStreamChunk.builder()
                            .error("Không thể kết nối tới chatbot service: " + error.getMessage())
                            .done(true)
                            .build());
                });
    }

    private Flux<ChatStreamChunk> parseSSEBlock(String block) {
        try {
            // Flask gửi raw JSON, không phải SSE format chuẩn
            // Mỗi block là một JSON object hoàn chỉnh
            String trimmed = block.trim();

            if (trimmed.isEmpty()) {
                return Flux.empty();
            }

            log.info("=== Parsing JSON block ({} chars): '{}'", trimmed.length(), trimmed);
            ChatStreamChunk chunk = objectMapper.readValue(trimmed, ChatStreamChunk.class);
            log.info("=== Successfully parsed: chunk='{}', done={}, error={}",
                    chunk.getChunk(), chunk.getDone(), chunk.getError());
            return Flux.just(chunk);
        } catch (Exception e) {
            log.error("=== Lỗi parse JSON block: '{}' - Error: {}", block, e.getMessage());
            return Flux.empty();
        }
    }
}
