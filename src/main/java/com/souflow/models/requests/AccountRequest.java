package com.souflow.models.requests;

import lombok.Data;

@Data
public class AccountRequest {
    private Long pk;
	private String username;
    private String password;
    private String fullname;
    private String email;
    private String photo;
    private String phone;
    private String address;
    private Boolean disabled;
    private RoleRequest roleRequest;
}
