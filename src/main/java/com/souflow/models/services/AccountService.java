package com.souflow.models.services;

import java.time.LocalDateTime;

import com.souflow.models.enums.RoleCode;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.AccountRequest;
import com.souflow.models.requests.AuthRequest;
import com.souflow.models.requests.ForgotPasswordRequest;
import com.souflow.models.requests.ResetPasswordRequest;
import com.souflow.models.requests.VerifyOtpRequest;
import com.souflow.models.responses.AccountResponse;
import com.souflow.models.responses.AuthResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.services.impl.AccountServiceImpl.GoogleTokenDTO;

public interface AccountService {
    void forgotPassword(ForgotPasswordRequest request);
    void verifyOtp(VerifyOtpRequest request);
    void resetPassword(ResetPasswordRequest request);
	void sendRegisterOtp(AccountRequest request);
	AuthResponse verifyRegisterOtp(VerifyOtpRequest request);
	AuthResponse register(AccountRequest request);
	AuthResponse login(AuthRequest authRequest);
	AuthResponse loginWithGoogle(GoogleTokenDTO googleToken);
	AuthResponse refreshToken(com.souflow.models.requests.RefreshTokenRequest request);
	AccountResponse save(AccountRequest request);
	void softDeleteByPk(Long accountPk);
	AccountResponse findByPk(Long accountPk);
	AccountResponse findByUsername(String username);
	AccountResponse findByEmail(String email);
	AccountResponse findAccountDetailByPk(Long accountPk);
	AccountResponse findAccountDetailByUsername(String username);
	AccountResponse findAccountDetailByEmail(String email);
	PageResponse<AccountResponse> filterAndPaginateAccounts(
		Boolean deleted, 
		String keyword, 
		LocalDateTime fromDate, 
		LocalDateTime toDate, 
		Boolean disabled, 
		RoleCode role,
        SortOrder sortOrder, 
		Integer pageNumber, 
		Integer pageSize);
}
