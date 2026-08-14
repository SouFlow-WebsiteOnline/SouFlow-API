package com.souflow.models.services;

import java.util.concurrent.CompletableFuture;

import com.souflow.models.requests.ChatMessageRequest;
import com.souflow.models.responses.ChatMessageResponse;

public interface ChatMessageService {
    
    public CompletableFuture<ChatMessageResponse> processMessage(ChatMessageRequest request);
}
