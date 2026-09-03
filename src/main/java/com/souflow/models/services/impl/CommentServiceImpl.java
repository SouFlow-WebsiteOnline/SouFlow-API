package com.souflow.models.services.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Comment;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.mappers.CommentMapper;
import com.souflow.models.repositories.CommentRepository;
import com.souflow.models.requests.CommentRequest;
import com.souflow.models.responses.CommentResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.services.CommentService;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepo;
	private final CommentMapper commentMapper;

	@Override
	@Transactional
	@CachePut(value = "commentList", key = "#result.pk")
    @CacheEvict(value = "commentPages", allEntries = true)
	public CommentResponse save(CommentRequest request) {
		Comment comment = commentMapper.toEntity(request);
		Comment saved = commentRepo.save(comment);
		return commentMapper.toBasicResponse(saved);
	}

	@Override
	@Transactional
	    @Caching(evict = {
    	@CacheEvict(value = "commentList", key = "#commentPk"),
    	@CacheEvict(value = "commentPages", allEntries = true)
    })
	public void softDeleteByPk(Long commentPk) {
		// TODO Auto-generated method stub
		commentRepo.softDelete(commentPk);
	}

	@Override
	@Cacheable(value = "commentList", key = "#commentPk")
	public CommentResponse findByPk(Long commentPk) {
		// TODO Auto-generated method stub
		if (commentPk == null ) throw new IllegalArgumentException("Can't not find comment when pk is null");
		Comment exist = commentRepo.findById(Long.valueOf(commentPk))
				.orElseThrow(() -> new EntityNotFoundException("Comment not found with Id: " + commentPk));
		return commentMapper.toDetailedResponse(exist);
	}
 
	@Override
	@Cacheable(value = "commentPages", key = "#keyword + '_' + #fromDate + '_' + #toDate + '_' + #deleted + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
	public PageResponse<CommentResponse> filterAndPaginateComments(String keyword, LocalDate fromDate, SortOrder sortOrder, LocalDate toDate, Boolean deleted, Integer pageNumber, Integer pageSize) {
		// TODO Auto-generated method stub
		Sort sort = sortOrder == SortOrder.ASC
	            ? Sort.by("id").ascending()
	            : Sort.by("id").descending();
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		Page<Comment> page = commentRepo.filterComments(keyword, fromDate, toDate, deleted, pageable);
		List<CommentResponse> responses = commentMapper.toDetailedResponseList(page.getContent());
		return new PageResponse<>(page, responses);
	}
}
