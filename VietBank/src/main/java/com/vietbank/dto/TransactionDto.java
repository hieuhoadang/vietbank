package com.vietbank.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.vietbank.enums.TransactionStatus;
import com.vietbank.enums.TransactionType;

import lombok.Data;

@Data
public class TransactionDto {
	private UUID id;
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String description;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
