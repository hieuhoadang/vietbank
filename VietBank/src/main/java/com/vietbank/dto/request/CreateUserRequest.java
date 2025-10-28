package com.vietbank.dto.request;

import java.time.LocalDate;

import com.vietbank.validator.IdCard;
import com.vietbank.validator.MinAge;
import com.vietbank.validator.Password;
import com.vietbank.validator.PhoneNumber;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {


	@NotBlank(message = "Full name is required")
    private String fullName;
	
    @NotBlank(message = "Phone number is required")
    @PhoneNumber(message = "{error.invalid.phone}")
    private String phoneNumber;
    
    @IdCard
    @NotBlank(message = "ID card is required")
    private String idCard;
    
    @Email(message = "{error.email.format}")
    @NotBlank(message = "Email is required")
    private String email;
    
    @Password
    @NotBlank(message = "Password is required")
    private String password;
    
    @MinAge(value = 16)
    private LocalDate dateOfBirth;
    
    private String address;
}
