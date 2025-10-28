package com.vietbank.dto.request;

import com.vietbank.enums.AccountType;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateBankAccountRequest {
	
	@Size(min = 10, max = 15, message = "accountNumber.size")
	private String accountNumber;
    private AccountType accountType;
}
