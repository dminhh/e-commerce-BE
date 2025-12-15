package com.prod.RAG.controllers;

import com.prod.RAG.model.ChatRequest;
import com.prod.RAG.model.ChatResponse;
import com.prod.RAG.services.IChatbotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@Slf4j
@RestController
@RequestMapping("/api/chatbot")
public class ChatController {

    @Autowired
    private IChatbotService chatbotService;

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("Nhận câu hỏi từ client: {}", request.getQuestion());

        ChatResponse response = chatbotService.chat(request);

        if (response.getSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




}
