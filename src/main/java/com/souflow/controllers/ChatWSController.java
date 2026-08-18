package com.souflow.controllers;

import org.springframework.stereotype.Controller;

import com.souflow.models.requests.ChatMessageRequest;
import com.souflow.models.services.ChatMessageService;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatWSController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void handleMessage(ChatMessageRequest request) {
        chatMessageService.processMessage(request)
                .thenAccept(response -> messagingTemplate.convertAndSend("/topic/chat", response))
                .exceptionally(ex -> {
                    messagingTemplate.convertAndSend("/topic/chat.errors", ex.getMessage());
                    return null;
                });
    }

}
