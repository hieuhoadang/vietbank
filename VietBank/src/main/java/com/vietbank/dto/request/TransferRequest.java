package com.vietbank.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequest {
	@NotNull(message = "From account ID is required")
	private String fromAccountId;
	@NotNull(message = "To account ID is required")
	private String toAccountId;
	@DecimalMin(value = "0.01", message = "error.invalid.amount")
	private BigDecimal amount;
	private String description;
}
