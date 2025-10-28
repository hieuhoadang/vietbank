package com.vietbank.controller;

import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vietbank.dto.BankAccountDto;
import com.vietbank.dto.ResponseSuccess;
import com.vietbank.dto.UserDto;
import com.vietbank.dto.request.CreateBankAccountRequest;
import com.vietbank.dto.request.CreateUserRequest;
import com.vietbank.dto.request.DepositRequest;
import com.vietbank.dto.request.SearchCustomerRequest;
import com.vietbank.dto.request.UpdateBankAccountRequest;
import com.vietbank.service.StaffService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequestMapping("/api/v1/staff")
@PreAuthorize("hasRole('STAFF')")
@RequiredArgsConstructor
public class StaffController {

	private final StaffService staffService;
	private final MessageSource messageSource;

	
	@PostMapping("/customers/search")
    public ResponseEntity<ResponseSuccess<Page<UserDto>>> searchCustomers(@Valid @RequestBody SearchCustomerRequest request) {
		Page<UserDto> customers = staffService.getCustomers(
	            request.getName(),
	            request.getPhone(),
	            request.getIdCard(),
	            request.getPage(),
	            request.getSize());
		
		ResponseSuccess<Page<UserDto>> response = ResponseSuccess.create("success.customer.search", customers, messageSource);
		return ResponseEntity.ok(response);
    }

    @PostMapping("/customers")
    public ResponseEntity<ResponseSuccess<UserDto>> createCustomer(@Valid @RequestBody CreateUserRequest request) {
        UserDto customer = staffService.createCustomer(request);
        ResponseSuccess<UserDto> response = ResponseSuccess.create("success.customer.create", customer, messageSource);
    	return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerId}")
    public ResponseEntity<ResponseSuccess<UserDto>> updateCustomer(@PathVariable String customerId, @Valid @RequestBody CreateUserRequest request) {
    	UserDto updatedCustomer = staffService.updateCustomer(customerId, request);
        ResponseSuccess<UserDto> response = ResponseSuccess.create(
                "success.customer.update",
                updatedCustomer,
                messageSource
        );
    	return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<ResponseSuccess<Void>> deleteCustomer(@PathVariable String customerId) {
        staffService.deleteCustomer(customerId);
        ResponseSuccess<Void> response = ResponseSuccess.create(
                "success.customer.delete",
                null,
                messageSource
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customers/{customerId}")
    public ResponseEntity<ResponseSuccess<UserDto>> getCustomer(@PathVariable String customerId) {
        UserDto customer = staffService.getCustomer(customerId);
        ResponseSuccess<UserDto> response = ResponseSuccess.create(
                "success.customer.get",
                customer,
                messageSource
        );
    	return ResponseEntity.ok(response);
    }

    @PostMapping("/customers/{customerId}/accounts")
    public ResponseEntity<ResponseSuccess<BankAccountDto>> createAccount(
            @PathVariable String customerId,
            @Valid
            @RequestBody CreateBankAccountRequest request) {
    	BankAccountDto account = staffService.createAccount(customerId, request);
    	ResponseSuccess<BankAccountDto> response = ResponseSuccess.create(
                "success.account.create",
                account,
                messageSource
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/customers/{customerId}/accounts/{accountId}")
    public ResponseEntity<ResponseSuccess<BankAccountDto>> updateAccount(
            @PathVariable String customerId,
            @PathVariable String accountId,
            @RequestBody UpdateBankAccountRequest request) {
    	BankAccountDto updatedAccount = 
    			staffService.updateAccount(customerId,
    										accountId,
    										request);
    	ResponseSuccess<BankAccountDto> response = ResponseSuccess.create(
                "success.account.update",
                updatedAccount,
                messageSource
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/customers/{customerId}/accounts/{accountId}/deposit")
    public ResponseEntity<ResponseSuccess<BankAccountDto>> deposit(
            @PathVariable String customerId,
            @PathVariable String accountId,
            @RequestBody DepositRequest request) {
    	BankAccountDto account = staffService.deposit(customerId, accountId, request.getAmount());
    	ResponseSuccess<BankAccountDto> response = ResponseSuccess.create(
                "success.account.deposit",
                account,
                messageSource
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/customers/{customerId}/accounts/{accountId}/deactivate")
    public ResponseEntity<ResponseSuccess<BankAccountDto>> deactivateAccount(@PathVariable String customerId, @PathVariable String accountId) {
    	BankAccountDto bankAccount = staffService.deactivateAccount(customerId, accountId);
    	ResponseSuccess<BankAccountDto> response = ResponseSuccess.create(
                "success.account.deactivate",
                bankAccount,
                messageSource
        );
    	return ResponseEntity.ok(response);
    }
    
    @PostMapping("/customers/{customerId}/accounts/{accountId}/activate")
    public ResponseEntity<ResponseSuccess<BankAccountDto>> activateAccount(@PathVariable String customerId, @PathVariable String accountId) {
    	BankAccountDto bankAccount = staffService.activateAccount(customerId, accountId);
    	ResponseSuccess<BankAccountDto> response = ResponseSuccess.create(
                "success.account.activate",
                bankAccount,
                messageSource
        );
    	return ResponseEntity.ok(response);
    }
}
