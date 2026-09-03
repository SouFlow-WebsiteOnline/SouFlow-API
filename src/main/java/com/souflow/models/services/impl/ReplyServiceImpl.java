package com.souflow.models.services.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.souflow.models.entities.Reply;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.mappers.ReplyMapper;
import com.souflow.models.repositories.ReplyRepository;
import com.souflow.models.requests.ReplyRequest;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.responses.ReplyResponse;
import com.souflow.models.services.ReplyService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplyServiceImpl implements ReplyService {
	
	private final ReplyRepository replyRepo;
	private final ReplyMapper replyMapper;

	@Override
	@Transactional
	@CachePut(value = "replyList", key = "#result.pk")
	@Caching(evict = {
		@CacheEvict(value = "replyPages", allEntries = true),
		@CacheEvict(value = "commentPages", allEntries = true)
	})
	public ReplyResponse save(ReplyRequest request) {
		Reply reply = replyMapper.toEntity(request);
		Reply saved = replyRepo.save(reply);
		return replyMapper.toResponse(saved);
	}

	@Override
	@Transactional
	@Caching(evict = {
		@CacheEvict(value = "replyList", key = "#replyPk"),
		@CacheEvict(value = "replyPages", allEntries = true),
		@CacheEvict(value = "commentPages", allEntries = true)
	})
	public void softDeleteByPk(Long replyPk) {
		replyRepo.softDelete(replyPk);
	}

	@Override
	public ReplyResponse findByPk(Long replyPk) {
		// TODO Auto-generated method stub
		if (replyPk == null) throw new IllegalArgumentException("Can't not find pk when reply is null");
		Reply exist = replyRepo.findById(Long.valueOf(replyPk))
				.orElseThrow(() -> new EntityNotFoundException("Reply not found with Id: " + replyPk));
		return replyMapper.toResponse(exist);
	}

	@Override
	public List<ReplyResponse> findAll() {
		// TODO Auto-generated method stub
		List<Reply> replies = replyRepo.findAll();
		return replyMapper.toResponseList(replies);
	}

	@Override
	@Cacheable(value = "replyPages", key = "#keyword + '_' + #fromDate + '_' + #toDate + '_' + #deleted + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
	public PageResponse<ReplyResponse> filterAndPaginateReply(
	        String keyword,
	        LocalDate fromDate,
	        LocalDate toDate,
	        Boolean deleted,
			SortOrder sortOrder,
	        Integer pageNumber,
			Integer pageSize) {
		
		Sort sort = sortOrder == SortOrder.ASC
	            ? Sort.by("id").ascending()
	            : Sort.by("id").descending();
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
	    Page<Reply> page = replyRepo.filterReplies(keyword, fromDate, toDate, deleted, pageable);
	    List<ReplyResponse> responses = replyMapper.toResponseList(page.getContent());
	    return new PageResponse<>(page, responses);
	}
}
