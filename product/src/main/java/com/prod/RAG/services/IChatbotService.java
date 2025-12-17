package com.prod.RAG.services;

import com.prod.RAG.model.ChatStreamChunk;
import reactor.core.publisher.Flux;

public interface IChatbotService {

    Flux<ChatStreamChunk> chatStream(String question);
}
