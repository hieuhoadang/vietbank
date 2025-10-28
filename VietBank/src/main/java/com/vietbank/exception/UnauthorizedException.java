package com.vietbank.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class UnauthorizedException extends CustomException {

	public UnauthorizedException(String errorCode) {
		super(errorCode, HttpStatus.UNAUTHORIZED);
		
	}
	
	
}
