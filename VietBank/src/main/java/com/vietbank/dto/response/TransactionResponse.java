package com.vietbank.dto.response;
import java.time.LocalDateTime;
import com.vietbank.enums.TransactionStatus;
import com.vietbank.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
@Data
public class TransactionResponse {
	
	@NotNull(message = "From account ID is required")
	private String fromAccountId;
	@NotNull(message = "To account ID is required")
	private String toAccountId;
	private TransactionType transactionType;
	private String description;
	private TransactionStatus status;
    private LocalDateTime createdAt;
}
