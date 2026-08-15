package com.souflow.controllers;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.souflow.models.enums.RoleCode;
import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.AccountRequest;
import com.souflow.models.requests.AuthRequest;
import com.souflow.models.responses.AccountResponse;
import com.souflow.models.responses.AuthResponse;
import com.souflow.models.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;
import com.souflow.models.services.impl.AccountServiceImpl.GoogleTokenDTO;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AccountService accountService;
    private final ImageService imageService;


    @PostMapping("/login")
    AuthResponse login(@RequestBody AuthRequest request) {
        return accountService.login(request);
    }

    @PostMapping("/google/login")
    AuthResponse googleLogin(@RequestBody GoogleTokenDTO token) {
        return accountService.loginWithGoogle(token);
    }

    @PostMapping("/account")
    AccountResponse save(
        @RequestPart("account") AccountRequest request,
        @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        
        if (file != null) {
            request.setPhoto(imageService.upload(file));
        }

        return accountService.save(request);
    }

    @DeleteMapping("/account/{pk}")
    void deleteAccount(@PathVariable Long pk) {
        accountService.softDeleteByPk(pk);
    }

    @GetMapping("/account/{pk}")
    AccountResponse findAccountByPk(@PathVariable Long pk) {
        return accountService.findByPk(pk);
    }

    @GetMapping("/account/by-username/{username}")
    AccountResponse findAccountByUsername(@PathVariable String username) {
        return accountService.findByUsername(username);
    }

    @GetMapping("/account/by-email/{email}")
    AccountResponse findAccountByEmail(@PathVariable String email) {
        return accountService.findByEmail(email);
    }



    @GetMapping("/account")
    PageResponse<AccountResponse> filterAndPaginateAccounts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) LocalDateTime fromDate,
        @RequestParam(required = false) LocalDateTime toDate,
        @RequestParam(defaultValue = "false") Boolean deleted,
        @RequestParam(defaultValue = "false") Boolean disabled,
        @RequestParam(defaultValue = "ALL") RoleCode role,
        @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
        @RequestParam(defaultValue = "0") Integer pageNumber,
        @RequestParam(defaultValue = "5") Integer pageSize
        ) {

        return accountService.filterAndPaginateAccounts(deleted, keyword, fromDate, toDate, disabled, role, sortOrder, pageNumber, pageSize);
    }
}
