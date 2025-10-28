package com.vietbank.exception;

import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
public class NotFoundException extends CustomException {

	public NotFoundException(String errorCode) {
		super(errorCode, HttpStatus.NOT_FOUND);
	}
	
	
}
