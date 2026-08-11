package com.souflow.models.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
	
	private String token;
	
	private String refreshToken;
	
	private String pk;
	
	private String fullname;
	
	private String email;
	
	private String photo;

	private String url;

	private String roleCode;

	private Boolean isNewUser;
}
