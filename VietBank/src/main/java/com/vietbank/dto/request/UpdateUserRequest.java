package com.vietbank.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class UpdateUserRequest {

	private String fullName;
    private String email;
    private LocalDate dateOfBirth;
    private String address;
}
