package com.vietbank.controller;

import java.util.List;

import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import com.vietbank.dto.ResponseSuccess;
import com.vietbank.dto.UserDto;
import com.vietbank.dto.request.TransferRequest;
import com.vietbank.dto.request.UpdateUserRequest;
import com.vietbank.dto.response.TransactionResponse;
import com.vietbank.entity.BankAccount;
import com.vietbank.entity.Transaction;
import com.vietbank.service.CustomerService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/customer")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class CustomerController {
	
	private final CustomerService customerService;
	private final MessageSource  messageSource;

	@GetMapping("/profile")
    public ResponseEntity<ResponseSuccess<UserDto>> getProfile() {
        UserDto userDto = customerService.getProfile();
        return ResponseEntity.ok(ResponseSuccess.create("success.profile.get", userDto, messageSource));
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseSuccess<UserDto>> updateProfile(@Valid
           @RequestBody UpdateUserRequest request) {
        UserDto userDto = customerService.updateProfile(request.getFullName(),
				request.getEmail(),
				request.getDateOfBirth(),
				request.getAddress());
        ResponseSuccess<UserDto> response = ResponseSuccess.create("success.profile.update", userDto, messageSource);
    	return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts")
    public ResponseEntity<ResponseSuccess< List<BankAccount> >> getAccounts() {
        List<BankAccount> accounts = customerService.getAccounts();
        ResponseSuccess<List<BankAccount>> response = ResponseSuccess.create("success.account.list", accounts, messageSource);
    	return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<ResponseSuccess<BankAccount>> getAccountById(@PathVariable String accountId) {
        BankAccount account = customerService.getAccountById(accountId);
        ResponseSuccess<BankAccount> response = ResponseSuccess.create("success.account.detail", account, messageSource);
    	return ResponseEntity.ok(response);
    }

    @PostMapping("/transfers")
    public ResponseEntity<ResponseSuccess<TransactionResponse>> transfer(@Valid
            @RequestBody TransferRequest request) {
    		Transaction transaction = customerService.transfer(
    				request.getFromAccountId(),
    				request.getToAccountId(),
    				request.getAmount(),
    				request.getDescription());
    		TransactionResponse responseData = new TransactionResponse();
    		responseData.setFromAccountId(transaction.getFromAccountId().getId());
    		responseData.setToAccountId(transaction.getToAccountId().getId());
    		responseData.setTransactionType(transaction.getTransactionType());
    		responseData.setDescription(transaction.getDescription());
    		responseData.setStatus(transaction.getStatus());
    		responseData.setCreatedAt(transaction.getCreatedAt());
    		
    		ResponseSuccess<TransactionResponse> response = ResponseSuccess.create("success.transfer.create", responseData, messageSource);
    		return ResponseEntity.ok(response);
    }

    @GetMapping("/transfers/{accountId}")
    public ResponseEntity<ResponseSuccess<List<Transaction>>> getTransactions(@PathVariable String accountId) {
    	List<Transaction> transactions = customerService.getTransactions(accountId);
    	ResponseSuccess<List<Transaction>> response = ResponseSuccess.create("success.transfer.list", transactions, messageSource);
    	return ResponseEntity.ok(response);
    }
}
