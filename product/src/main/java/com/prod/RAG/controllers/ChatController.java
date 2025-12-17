package com.prod.RAG.controllers;

import com.prod.RAG.model.ChatQuestion;
import com.prod.RAG.model.ChatStreamChunk;
import com.prod.RAG.services.IChatbotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/chatbot")
public class ChatController {

    @Autowired
    private IChatbotService chatbotService;

    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<ChatStreamChunk>> chatStream(@RequestBody ChatQuestion request) {
        String question = request.getQuestion();

        if (question == null || question.trim().isEmpty()) {
            return Flux.just(ServerSentEvent.<ChatStreamChunk>builder()
                    .data(ChatStreamChunk.builder()
                            .error("Câu hỏi không được để trống")
                            .done(true)
                            .build())
                    .build());
        }

        log.info("Nhận câu hỏi streaming từ client: {}", question);

//        return chatbotService.chatStream(question)
//                .map(chunk -> ServerSentEvent.<ChatStreamChunk>builder()
//                        .data(chunk)
//                        .build())
//                .doOnComplete(() -> log.info("Stream hoàn tất cho câu hỏi: {}", question))
//                .doOnError(error -> log.error("Lỗi khi stream: ", error));
        return chatbotService.chatStream(question)
                .doOnSubscribe(sub -> log.info("=== Client đã subscribe stream"))
                .doOnNext(streamChunk -> log.info("=== CONTROLLER GỬI chunk: chunk='{}', done={}, error={}",
                        streamChunk.getChunk(), streamChunk.getDone(), streamChunk.getError()))
                .map(streamChunk -> ServerSentEvent.<ChatStreamChunk>builder()
                        .data(streamChunk)
                        .build())
                .doOnComplete(() -> log.info("=== KẾT THÚC: Stream hoàn tất cho: {}", question))
                .doOnError(error -> log.error("=== LỖI khi stream: ", error))
                .doOnCancel(() -> log.warn("=== HỦY: Client đã cancel stream"));
    }
}
