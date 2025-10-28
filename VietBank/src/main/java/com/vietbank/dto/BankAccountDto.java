package com.vietbank.dto;

import java.math.BigDecimal;

import com.vietbank.enums.AccountStatus;
import com.vietbank.enums.AccountType;

import lombok.Data;

@Data
public class BankAccountDto {
	private String id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private String userId;
    private AccountStatus active;
}
