package com.vietbank.dto;

import java.time.LocalDate;
import com.vietbank.enums.Role;

import lombok.Data;

@Data
public class UserDto {

	 	private String id;
	    private Role role;
	    private String fullName;
	    private String phoneNumber;
	    private String idCard;
	    private String email;
	    private LocalDate dateOfBirth;
	    private String address;
	    private boolean isActive;
}
