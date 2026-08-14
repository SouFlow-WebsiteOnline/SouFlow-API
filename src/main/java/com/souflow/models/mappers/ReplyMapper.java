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
import com.souflow.models.entities.Comment;
import com.souflow.models.entities.Reply;
import com.souflow.models.repositories.ReplyRepository;
import com.souflow.models.requests.ReplyRequest;
import com.souflow.models.responses.ReplyResponse;

import jakarta.persistence.EntityNotFoundException;

@Component
@Mapper(componentModel = "spring")
public abstract class ReplyMapper {
    
    @Autowired
    protected ReplyRepository replyRepo;

    @Mapping(target = "createdDate",    ignore = true)
    @Mapping(target = "comment",        ignore = true)
    @Mapping(target = "account",        ignore = true)
    @Mapping(target = "deleted",        ignore = true)
    public abstract Reply toEntity(ReplyRequest request); 

    @Mapping(target = "createdDate", 	source = "createdDate", 			dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "username",       source = "account.username")
    @Mapping(target = "fullname",       source = "account.fullname")
    @Mapping(target = "photo",          source = "account.photo")
    @Mapping(target = "accountPk",      source = "account.pk")
    @Mapping(target = "role",           source = "account.role.code")
    @Mapping(target = "commentPk",      source = "comment.pk")
    public abstract ReplyResponse toResponse(Reply reply);

    public abstract List<Reply> toEntityList(List<ReplyRequest> replyRequests);

    public abstract List<ReplyResponse> toResponseList(List<Reply> replies);

    @AfterMapping
    protected void afterToEntity(ReplyRequest request, @MappingTarget Reply reply) {
        Long pk = reply.getPk();
        if (pk != null) {
            Reply oldReply = replyRepo.findById(pk).orElseThrow(
                () -> new EntityNotFoundException("Reply not found with pk: " + pk)
            );
            reply.setCreatedDate(oldReply.getCreatedDate());
            reply.setComment(oldReply.getComment());
            reply.setAccount(oldReply.getAccount());
            reply.setDeleted(oldReply.getDeleted());
            return;
        }
        reply.setCreatedDate(LocalDateTime.now());
        Comment comment = new Comment();
        Account account = new Account();
        comment.setPk(request.getCommentPk());
        account.setPk(request.getAccountPk());
        reply.setComment(comment);
        reply.setAccount(account);
        reply.setDeleted(false);
    }
}
