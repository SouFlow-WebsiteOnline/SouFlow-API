package com.souflow.models.services.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.souflow.models.entities.ChatMessage;
import com.souflow.models.mappers.ChatMessageMapper;
import com.souflow.models.repositories.ChatMessageRepository;
import com.souflow.models.requests.ChatMessageRequest;
import com.souflow.models.responses.ChatMessageResponse;
import com.souflow.models.services.ChatMessageService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    public final ChatMessageMapper chatMessageMapper;

    public final ChatMessageRepository chatMessageRepo;

    public CompletableFuture<ChatMessageResponse> processMessage(ChatMessageRequest request) {
        // your existing chat logic — save message, call LLM, etc.
        ChatMessageResponse response = save(request);
        return CompletableFuture.completedFuture(response);
    }

    @Async("chatTaskExecutor")
    public ChatMessageResponse save(ChatMessageRequest request) {
        ChatMessage message = chatMessageMapper.toEntity(request);
        return chatMessageMapper.toResponse(chatMessageRepo.save(message));
    }
}
