package com.vietbank.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

@Data
public class DepositRequest {

	@DecimalMin(value = "0.01", message = "{error.invalid.amount}")
    private BigDecimal amount;
}
