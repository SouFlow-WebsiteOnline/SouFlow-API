package com.souflow.models.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.souflow.models.entities.Account;
import com.souflow.models.entities.ChatMessage;
import com.souflow.models.repositories.AccountRepository;
import com.souflow.models.repositories.ChatMessageRepository;
import com.souflow.models.requests.ChatMessageRequest;
import com.souflow.models.responses.ChatMessageResponse;
import com.souflow.models.services.ImageService;

import jakarta.persistence.EntityNotFoundException;

@Component
@Mapper(componentModel = "spring")
public abstract class ChatMessageMapper {

    @Autowired
    protected ImageService imageService;

    @Autowired
    protected ChatMessageRepository chatMessageRepo;

    @Autowired
    protected AccountRepository accountRepo;

    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "account", ignore = true)
    public abstract ChatMessage toEntity(ChatMessageRequest request);

    @Mapping(target = "username",         source = "account.username")
    @Mapping(target = "fullname",         source = "account.fullname")
    @Mapping(target = "accountUrl",       ignore = true)
    public abstract ChatMessageResponse toResponse(ChatMessage chatMessage);

    public abstract List<ChatMessageResponse> toResponseList(List<ChatMessage> chatMessages);

    @AfterMapping
    protected void afterToEntity(ChatMessageRequest request, @MappingTarget ChatMessage chatMessage) {

        if (request.getPk() != null) {
            Account account = accountRepo.findById(request.getAccountPk()).orElseThrow(
                () -> new EntityNotFoundException("Account not found with pk: " + request.getAccountPk())
            );
            chatMessage.setCreatedDate(account.getCreatedDate());
            chatMessage.setAccount(account);
        }

        chatMessage.setCreatedDate(LocalDateTime.now());
        chatMessage.getAccount().setPk(request.getAccountPk());
    }

    @AfterMapping
    protected void afterToResponse(ChatMessage chatMessage, @MappingTarget ChatMessageResponse response) {

        try {
            String url = imageService.getPublicUrl(chatMessage.getAccount().getPhoto());
            response.setAccountUrl(url);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }
}
