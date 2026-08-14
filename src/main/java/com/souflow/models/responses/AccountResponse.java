package com.souflow.models.responses;

import java.util.List;

import lombok.Data;

@Data
public class AccountResponse {
	
	private String pk;
	
	private String username;
	
	private String fullname;
	
	private String email;
	
	private String photo;

	private String url;
	
	private String address;
	
	private String phone;
	
	private String createdDate;
	
	private String credentialExpiredDate;
	
	private String credentialExpired;
	
	private String disabled;

	private RoleResponse roleResponse;
	
	private List<OrderResponse> orderResponses;
	
	private List<CartResponse> cartResponses;

	private List<ChatMessageResponse> chatMessageResponses;
}
