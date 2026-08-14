package com.souflow.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.souflow.models.entities.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
}
