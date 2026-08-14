package com.souflow.models.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.souflow.models.entities.Account;
import com.souflow.models.repositories.AccountRepository;
import com.souflow.models.requests.AccountRequest;
import com.souflow.models.responses.AccountResponse;
import com.souflow.models.services.ImageService;

import jakarta.persistence.EntityNotFoundException;

@Mapper(componentModel = "spring", uses = {OrderMapper.class, CartMapper.class, RoleMapper.class, ChatMessageMapper.class})
public abstract class AccountMapper {
	
	@Autowired
	protected AccountRepository accountRepo;
	
	@Autowired
	protected PasswordEncoder passwordEncoder;

	@Autowired
	protected ImageService imageService;

	@Mapping(target = "deleted",				ignore = true)
	@Mapping(target = "createdDate", 			ignore = true)
	@Mapping(target = "credentialExpired", 		ignore = true)
	@Mapping(target = "credentialExpiredDate", 	ignore = true)
	@Mapping(target = "carts", 					ignore = true)
	@Mapping(target = "orders", 				ignore = true)
	@Mapping(target = "chatMessages",			ignore = true)			
	@Mapping(target = "role",					source = "roleRequest")
	public abstract Account toEntity(AccountRequest request); 
	
	@Mapping(target = "createdDate", 				source = "createdDate", 			dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "credentialExpiredDate", 		source = "credentialExpiredDate", 	dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "roleResponse", 				source = "role")
	@Mapping(target = "url",						ignore = true)
	@Mapping(target = "cartResponses", 				ignore = true)
	@Mapping(target = "orderResponses", 			ignore = true)
	@Mapping(target = "chatMessageResponses", 		source = "chatMessages")
	@Named("basicResponse")
	public abstract AccountResponse toBasicResponse(Account account);

	@AfterMapping
	protected void afterToBasicResponse(@MappingTarget AccountResponse response) {
		try {
			if (imageService != null) {
				String url = imageService.getPublicUrl(response.getPhoto());
				response.setUrl(url);
			}
		} catch (Exception e) {}
	}

	@Mapping(target = "url", 					ignore = true)
	@Mapping(target = "roleResponse", 			source = "role")
	@Mapping(target = "cartResponses", 			source = "carts")
	@Mapping(target = "orderResponses", 		source = "orders")
	@Mapping(target = "chatMessageResponses",	source = "chatMessages")
	@Named("detailedResponse")
	public abstract AccountResponse toDetailResponse(Account account);

	@IterableMapping(qualifiedByName = "basicResponse")
	public abstract List<AccountResponse> toBasicResponseList(List<Account> accounts);

	@IterableMapping(qualifiedByName = "detailedResponse")
	public abstract List<AccountResponse> toDetailedResponseList(List<Account> account); 
	
	@AfterMapping
    protected void afterToEntity(AccountRequest request, @MappingTarget Account account) {
		Long pk = request.getPk();
		if (pk != null) { //update
			Account oldAccount = accountRepo.findById(pk)
							.orElseThrow(() -> new EntityNotFoundException("Account not found with pk: " + pk));
			String password = request.getPassword();
			
			if (password != null) {
				account.setPassword(passwordEncoder.encode(request.getPassword()));
				account.setCredentialExpiredDate(LocalDateTime.now().plusMonths(3));
				account.setCredentialExpired(false);
			} else {
				account.setPassword(oldAccount.getPassword());
				account.setCredentialExpired(oldAccount.getCredentialExpired());
				account.setCredentialExpiredDate(oldAccount.getCredentialExpiredDate());
			}

			if (request.getPhoto() == null || request.getPhoto().isBlank()) {
				account.setPhoto(oldAccount.getPhoto());
			}

			if (request.getEmail() == null || request.getEmail().isBlank()) {
				account.setEmail(oldAccount.getEmail());
			}
			
			if (request.getRoleRequest() == null) {
				account.setRole(oldAccount.getRole());
			}

			account.setCreatedDate(oldAccount.getCreatedDate());
			account.setDisabled(request.getDisabled() == null ? oldAccount.getDisabled() : request.getDisabled());
			account.setDeleted(oldAccount.getDeleted());
			return;
		}
		// new account — must have a password
		if (request.getPassword() != null && !request.getPassword().isBlank()) {
			account.setPassword(passwordEncoder.encode(request.getPassword()));
		} else {
			throw new IllegalArgumentException("Password is required for new accounts");
		}
		account.setDisabled(request.getDisabled() == null ? false : request.getDisabled());
        account.setCreatedDate(LocalDateTime.now());
		account.setCredentialExpiredDate(LocalDateTime.now().plusMonths(3));
		account.setCredentialExpired(false);
		account.setDeleted(false);
    }

	@AfterMapping
	protected void afterToResponse(@MappingTarget AccountResponse response) {
		try {
			String url = imageService.getPublicUrl(response.getPhoto());
			response.setUrl(url);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}

