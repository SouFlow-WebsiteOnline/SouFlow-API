package com.souflow.models.services;

import java.time.LocalDate;
import java.util.List;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.ReplyRequest;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.responses.ReplyResponse;

public interface ReplyService {
	ReplyResponse save(ReplyRequest request);
	void softDeleteByPk(Long replyPk);
	ReplyResponse findByPk(Long replyPk);
	List<ReplyResponse> findAll();
	PageResponse<ReplyResponse> filterAndPaginateReply(
			String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            Boolean deleted,
			SortOrder sortOrder,
            Integer pageNumber,
			Integer pageSize
	);
}
