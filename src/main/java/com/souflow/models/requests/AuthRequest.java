package com.souflow.models.requests;

import lombok.Data;

@Data
public class AuthRequest {

	private String username;
	
	private String password;

	private Boolean rememberMe;
}
