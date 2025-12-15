package com.prod.RAG.services;

import com.prod.RAG.model.ChatRequest;
import com.prod.RAG.model.ChatResponse;

public interface IChatbotService {

    ChatResponse chat(ChatRequest request);

}
