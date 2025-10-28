package com.vietbank.dto.request;

import com.vietbank.validator.Password;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {
	
	@NotBlank(message = "error.token.required")
	private String token;
	
	@NotBlank(message = "error.password.required")
	@Password
	private String newPassword;
	
}
