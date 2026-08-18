package com.souflow.models.services;

import java.time.LocalDate;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.CommentRequest;
import com.souflow.models.responses.CommentResponse;
import com.souflow.models.responses.PageResponse;

public interface CommentService {
	CommentResponse save(CommentRequest request);
	void softDeleteByPk(Long commentPk);
	CommentResponse findByPk(Long commentPk);
	PageResponse<CommentResponse> filterAndPaginateComments(
		String keyword, 
		LocalDate fromDate,
		SortOrder sortOrder, 
		LocalDate toDate, 
		Boolean deleted, 
		Integer pageNumber, 
		Integer pageSize);
}
