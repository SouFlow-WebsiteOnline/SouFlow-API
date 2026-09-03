package com.souflow.models.services.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.souflow.models.entities.Account;
import com.souflow.models.entities.Role;
import com.souflow.models.enums.RoleCode;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.mappers.AccountMapper;
import com.souflow.models.repositories.AccountRepository;
import com.souflow.models.repositories.RoleRepository;
import com.souflow.models.requests.AccountRequest;
import com.souflow.models.requests.AuthRequest;
import com.souflow.models.requests.ForgotPasswordRequest;
import com.souflow.models.requests.ResetPasswordRequest;
import com.souflow.models.requests.VerifyOtpRequest;
import com.souflow.models.responses.AccountResponse;
import com.souflow.models.responses.AuthResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.services.AccountService;
import com.souflow.models.services.ImageService;
import com.souflow.utils.JwtUtil;
import com.souflow.models.services.RefreshTokenService;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.CacheManager;
import com.souflow.models.enums.OtpValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {
	
	private final GoogleAuthService googleAuthService;
	private final AccountRepository accountRepo;
	private final RoleRepository roleRepo;
	private final AccountMapper accountMapper;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final ImageService imageService;
	private final PasswordEncoder passwordEncoder;
	private final CacheManager cacheManager;
	private final OtpService otpService;
	private final EmailService emailService;
	private final RefreshTokenService refreshTokenService;
	private final com.souflow.models.services.SystemLogService systemLogService;
	
	private void checkOtpValidationResult(OtpValidationResult result, String email) {
		switch (result) {
			case SUCCESS -> {}
			case INVALID_OTP -> throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Mã OTP không chính xác. Vui lòng kiểm tra lại!");
			case MAX_ATTEMPTS_EXCEEDED -> {
				long minutes = otpService.getRemainingBlockMinutes(email);
				throw new ResponseStatusException(
						HttpStatus.TOO_MANY_REQUESTS,
						"Bạn đã nhập sai mã OTP quá 5 lần. Email tạm thời bị khóa xác thực trong " + minutes + " phút!");
			}
			case EXPIRED_OR_NOT_FOUND -> throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ hoặc đã hết hạn!");
		}
	}

	@Override
	public void sendRegisterOtp(AccountRequest request) {
		String username = request.getUsername() != null ? request.getUsername().trim() : "";
		String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

		if (username.isBlank() || email.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên tài khoản và email không được để trống!");
		}

		if (otpService.isBlocked(email)) {
			long minutes = otpService.getRemainingBlockMinutes(email);
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Email này đang bị tạm khóa xác thực OTP do nhập sai quá nhiều lần. Vui lòng thử lại sau " + minutes + " phút!");
		}

		if (accountRepo.findByUsername(username).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên tài khoản này đã được sử dụng!");
		}
		if (accountRepo.findByEmail(email).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email này đã được sử dụng!");
		}

		if (otpService.isCooldownActive(email)) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã OTP!");
		}

		request.setUsername(username);
		request.setEmail(email);

		String otp = otpService.generateRegisterOtp(email, request);
		try {
			emailService.sendRegisterOtpEmail(email, otp);
		} catch (Exception e) {
			log.error("Lỗi khi gửi mã OTP đăng ký tới {}", email, e);
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi gửi mã OTP qua email");
		}
	}

	@Override
	@Transactional
	public AuthResponse verifyRegisterOtp(VerifyOtpRequest request) {
		String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
		String otp = request.getOtp() != null ? request.getOtp().trim() : "";

		if (otpService.isBlocked(email)) {
			long minutes = otpService.getRemainingBlockMinutes(email);
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Email này đang bị tạm khóa xác thực OTP. Vui lòng thử lại sau " + minutes + " phút!");
		}

		OtpValidationResult validationResult = otpService.validateRegisterOtp(email, otp);
		checkOtpValidationResult(validationResult, email);

		AccountRequest registerData = otpService.getRegisterData(email);
		if (registerData == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Thông tin đăng ký đã hết hạn. Vui lòng thực hiện lại!");
		}

		String username = registerData.getUsername() != null ? registerData.getUsername().trim() : "";
		if (accountRepo.findByUsername(username).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên tài khoản này đã được sử dụng!");
		}
		if (accountRepo.findByEmail(email).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email này đã được sử dụng!");
		}

		Role role = roleRepo.findByCode(RoleCode.USER)
				.orElseThrow(() -> new EntityNotFoundException("Role USER not found"));

		registerData.setUsername(username);
		registerData.setEmail(email);
		Account account = accountMapper.toEntity(registerData);
		account.setPassword(passwordEncoder.encode(registerData.getPassword()));
		account.setRole(role);
		account.setCreatedDate(LocalDateTime.now());

		try {
			account = accountRepo.save(account);
		} catch (org.springframework.dao.DataIntegrityViolationException ex) {
			log.warn("Trùng lặp tài khoản khi đăng ký đồng thời: username={}, email={}", username, email);
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Tài khoản hoặc email này đã tồn tại trên hệ thống!");
		}

		clearAccountCaches(account);
		otpService.deleteRegisterData(email);

		String token = jwtUtil.generateToken(account.getUsername(), account.getRole().getCode().name());
		String refreshToken = refreshTokenService.generateAndSaveRefreshToken(account.getUsername(), false);

		return AuthResponse.builder()
				.token(token)
				.refreshToken(refreshToken)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.roleCode(account.getRole().getCode().name())
				.build();
	}

	@Override
	@Transactional
	public AuthResponse register(AccountRequest request) {
		if (accountRepo.findByUsername(request.getUsername()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
		}
		if (accountRepo.findByEmail(request.getEmail()).isPresent()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
		}

		Role role = roleRepo.findByCode(RoleCode.USER)
				.orElseThrow(() -> new EntityNotFoundException("Role USER not found"));

		Account account = accountMapper.toEntity(request);
		account.setPassword(passwordEncoder.encode(request.getPassword()));
		account.setRole(role);
		account.setCreatedDate(LocalDateTime.now());
		account = accountRepo.save(account);
		clearAccountCaches(account);

		String token = jwtUtil.generateToken(account.getUsername(), account.getRole().getCode().name());
		String refreshToken = refreshTokenService.generateAndSaveRefreshToken(account.getUsername(), false);

		return AuthResponse.builder()
				.token(token)
				.refreshToken(refreshToken)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.roleCode(account.getRole().getCode().name())
				.build();
	}

	@Override
	@Transactional
	public AuthResponse login(AuthRequest authRequest) {
		// TODO Auto-generated method stub
		try {
			// Check username and password under the hood
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					authRequest.getUsername(),
					authRequest.getPassword()
				)
			);
		} catch (AuthenticationException e) {
			if (e instanceof org.springframework.security.authentication.DisabledException) {
				throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tài khoản của bạn đã bị khoá");
			}
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
		}
		
		// If spring security said ok then generate token
		Account account = accountRepo.findByUsername(authRequest.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("Username not found: " + authRequest.getUsername()));
		
		String token = jwtUtil.generateToken(account.getUsername(), account.getRole().getCode().name());
		
		boolean rememberMe = authRequest.getRememberMe() != null && authRequest.getRememberMe();
		String refreshToken = refreshTokenService.generateAndSaveRefreshToken(account.getUsername(), rememberMe);
		
		// Send it back to frontend
		try {
			systemLogService.log("AUTH", "LOGIN_PASSWORD", account.getUsername(), account.getRole().getCode().name(), account.getUsername(), "Đăng nhập tài khoản / mật khẩu thành công", null);
			return AuthResponse.builder()
				.token(token)
				.refreshToken(refreshToken)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.roleCode(account.getRole().getCode().name())
				.url(imageService.getPublicUrl(account.getPhoto()))
				.build();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return null;
		}

	}
	
	@Override
	@Transactional
	public AuthResponse loginWithGoogle(GoogleTokenDTO googleToken) {
		// TODO Auto-generated method stub 
        try {
            // 1. Verify Google token
            GoogleIdToken.Payload payload = googleAuthService.verify(googleToken.get());

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            Account account = accountRepo.findByEmail(email)
            		.orElse(null);
			
			if (account == null) {
				// Return special response for new user
				return AuthResponse.builder()
					.isNewUser(true)
					.email(email)
					.fullname(name)
					.build();
			}
			
            // Generate JWT for existing user
            String token = jwtUtil.generateToken(
                account.getUsername(),
                account.getRole().getCode().name()
            );
            String refreshToken = refreshTokenService.generateAndSaveRefreshToken(account.getUsername(), true);

            systemLogService.log("AUTH", "LOGIN_GOOGLE", account.getUsername(), account.getRole().getCode().name(), account.getEmail(), "Đăng nhập hệ thống qua Google OAuth thành công", null);

            return AuthResponse.builder()
				.token(token)
				.refreshToken(refreshToken)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.roleCode(account.getRole().getCode().name())
				.isNewUser(false)
				.build();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
        }
	}

	@Override
	@Transactional
	public AuthResponse refreshToken(com.souflow.models.requests.RefreshTokenRequest request) {
		String token = request.getRefreshToken();
		String username = refreshTokenService.validateAndGetUsername(token);
		
		if (username == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
		}
		
		Account account = accountRepo.findByUsername(username)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
		
		// Delete old refresh token
		refreshTokenService.deleteRefreshToken(token);
		
		// Generate new tokens
		String newAccessToken = jwtUtil.generateToken(account.getUsername(), account.getRole().getCode().name());
		// Defaulting to 7 days for refreshed token to avoid keeping state of rememberMe, 
		// but since they refresh it, it means they are active or they have persistent cookie.
		// Actually, if it's a short session, refreshing it should keep it alive for 12 hours.
		// We'll just pass true here for simplicity, or ideally read the old TTL from Redis. 
		// But Redis TTL can't easily be read and matched. Let's just use true since if they use rememberMe=false,
		// the cookie is deleted on browser close anyway!
		String newRefreshToken = refreshTokenService.generateAndSaveRefreshToken(account.getUsername(), true);
		
		return AuthResponse.builder()
				.token(newAccessToken)
				.refreshToken(newRefreshToken)
				.pk(String.valueOf(account.getPk()))
				.fullname(account.getFullname())
				.email(account.getEmail())
				.photo(account.getPhoto())
				.roleCode(account.getRole().getCode().name())
				.build();
	}

	@Override
	public void forgotPassword(ForgotPasswordRequest request) {
		String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
		if (email.isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email không được để trống!");
		}

		if (otpService.isBlocked(email)) {
			long minutes = otpService.getRemainingBlockMinutes(email);
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Email này đang bị tạm khóa xác thực OTP do nhập sai quá nhiều lần. Vui lòng thử lại sau " + minutes + " phút!");
		}

		if (otpService.isCooldownActive(email)) {
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã OTP!");
		}

		Optional<Account> accountOpt = accountRepo.findByEmail(email);
		if (accountOpt.isPresent()) {
			String otp = otpService.generateOtp(email);
			try {
				emailService.sendOtpEmail(email, otp);
			} catch (Exception e) {
				log.error("Lỗi khi gửi email OTP quên mật khẩu tới {}", email, e);
			}
		} else {
			// Set cooldown for non-existing email to prevent high-frequency scanning
			otpService.setCooldown(email);
			// Random small delay to mitigate timing-based user enumeration attacks
			try {
				Thread.sleep(100 + (long)(Math.random() * 80));
			} catch (InterruptedException ignored) {}
		}
		// Trả về 200 generic message chuẩn OWASP phòng tránh dò email (User Enumeration Prevention)
	}

	@Override
	public void verifyOtp(VerifyOtpRequest request) {
		String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
		String otp = request.getOtp() != null ? request.getOtp().trim() : "";

		if (otpService.isBlocked(email)) {
			long minutes = otpService.getRemainingBlockMinutes(email);
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Email này đang bị tạm khóa xác thực OTP. Vui lòng thử lại sau " + minutes + " phút!");
		}

		OtpValidationResult validationResult = otpService.verifyOtpWithoutDeleting(email, otp);
		checkOtpValidationResult(validationResult, email);
	}

	@Override
	@Transactional
	public void resetPassword(ResetPasswordRequest request) {
		String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
		String otp = request.getOtp() != null ? request.getOtp().trim() : "";

		if (otpService.isBlocked(email)) {
			long minutes = otpService.getRemainingBlockMinutes(email);
			throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
					"Email này đang bị tạm khóa xác thực OTP. Vui lòng thử lại sau " + minutes + " phút!");
		}

		OtpValidationResult validationResult = otpService.validateOtp(email, otp);
		checkOtpValidationResult(validationResult, email);
		
		Account account = accountRepo.findByEmail(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản với email này"));
		
		account.setPassword(passwordEncoder.encode(request.getNewPassword()));
		accountRepo.save(account);
		clearAccountCaches(account);
	}
	
	@Override
	@Transactional
	public AccountResponse save(AccountRequest request) {
		Account account = accountMapper.toEntity(request);
		Account saved = accountRepo.save(account);
		clearAccountCaches(saved);
		return accountMapper.toBasicResponse(saved);
	}

	@Override
	@Transactional
	public void softDeleteByPk(Long accountPk) {
		accountRepo.softDelete(accountPk);
		Account account = accountRepo.findById(accountPk).orElse(null);
		if (account != null) {
		    clearAccountCaches(account);
		}
	}
	
	private void clearAccountCaches(Account account) {
	    if (cacheManager != null) {
	        org.springframework.cache.Cache pagesCache = cacheManager.getCache("accountPages");
	        if (pagesCache != null) pagesCache.clear();
	        
	        org.springframework.cache.Cache listCache = cacheManager.getCache("accountList");
	        if (listCache != null) {
	            if (account.getPk() != null) listCache.evict(account.getPk());
	            if (account.getUsername() != null) listCache.evict(account.getUsername());
	            if (account.getEmail() != null) listCache.evict(account.getEmail());
	        }
	        
	        org.springframework.cache.Cache detailsCache = cacheManager.getCache("accountDetailsList");
	        if (detailsCache != null) {
	            if (account.getPk() != null) detailsCache.evict(account.getPk());
	            if (account.getUsername() != null) detailsCache.evict(account.getUsername());
	            if (account.getEmail() != null) detailsCache.evict(account.getEmail());
	        }
	    }
	}

	@Override
	@Cacheable(value = "accountList", key = "#accountPk")
	public AccountResponse findByPk(Long accountPk) {
		// TODO Auto-generated method stub
		if (accountPk == null) throw new IllegalArgumentException("Can't find account when pk is null");
		Account exist = accountRepo.findById(accountPk)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with username: " + accountPk));
		return accountMapper.toBasicResponse(exist);
	}

	@Override
	@Cacheable(value = "accountList", key = "#username")
	public AccountResponse findByUsername(String username) {
		// TODO Auto-generated method stub
		Account exist = accountRepo.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with username: " + username));
		return accountMapper.toBasicResponse(exist);
	}

	@Override
	@Cacheable(value = "accountList", key = "#email")
	public AccountResponse findByEmail(String email) {
		// TODO Auto-generated method stub
		Account exist = accountRepo.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with email: " + email));
		return accountMapper.toBasicResponse(exist);
	}

	@Override
	@Cacheable(value = "accountDetailsList", key = "#accountPk")
	public AccountResponse findAccountDetailByPk(Long accountPk) {
		// TODO Auto-generated method stub
		if (accountPk == null) throw new IllegalArgumentException("Can't find account when pk is null");
		Account exist = accountRepo.findById(accountPk)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with username: " + accountPk));
		return accountMapper.toDetailResponse(exist);
	}

	@Override
	@Cacheable(value = "accountList", key = "#username")
	public AccountResponse findAccountDetailByUsername(String username) {
		// TODO Auto-generated method stub
		Account exist = accountRepo.findByUsername(username)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with username: " + username));
		return accountMapper.toDetailResponse(exist);
	}

	@Override
	@Cacheable(value = "accountList", key = "#email")
	public AccountResponse findAccountDetailByEmail(String email) {
		// TODO Auto-generated method stub
		Account exist = accountRepo.findByEmail(email)
				.orElseThrow(() -> new EntityNotFoundException("Account not found with email: " + email));
		return accountMapper.toDetailResponse(exist);
	}

	@Override
	@Cacheable(value = "accountPages", key = "#deleted + '_' + #keyword + '_' + #fromDate + '_' + #toDate + '_' + #disabled + '_' + #role  + '_' + #sortOrder + '_' + #pageNumber + '_' + #pageSize")
	public PageResponse<AccountResponse> filterAndPaginateAccounts(Boolean deleted, String keyword, LocalDateTime fromDate, LocalDateTime toDate, Boolean disabled, RoleCode role, SortOrder sortOrder, Integer pageNumber, Integer pageSize) {
		// TODO Auto-generated method stub
		accountRepo.checkAndExpireCredentialBeforePagination(deleted, keyword, fromDate, toDate, disabled, role);
		Sort sort = sortOrder == SortOrder.ASC
	            ? Sort.by("id").ascending()
	            : Sort.by("id").descending();
		Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
		String sanitizedKeyword = com.souflow.utils.StringUtil.sanitizeSqlLikeKeyword(keyword);
		Page<Account> page = accountRepo.filterAccounts(deleted, sanitizedKeyword, fromDate, toDate, disabled, role, pageable);
		List<AccountResponse> responses = accountMapper.toBasicResponseList(page.getContent());
		return new PageResponse<>(page, responses);
	}

	@lombok.Data
	public static class GoogleTokenDTO {
		private String token;
		public String get() { return token; }
	}
}
