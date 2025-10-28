package com.vietbank.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class CustomException extends RuntimeException {
	
	private final HttpStatus status;
	private final String errorCode;

	public CustomException(String errorCode,HttpStatus status) {
		super(errorCode);
		this.status = status;
		this.errorCode = errorCode;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public String getErrorCode() {
		return errorCode;
	}
	
	
	
	
}
