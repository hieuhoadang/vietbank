package com.vietbank.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
	
	@Email(message = "error.email.invalid")
	private String email;
}
