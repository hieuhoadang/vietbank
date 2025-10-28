package com.vietbank.config;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vietbank.dto.response.ErrorResponse;
import com.vietbank.exception.CustomException;

import jakarta.validation.ConstraintViolation;

@RestControllerAdvice
public class ExceptionHandlerConfig {
	
	private final MessageSource messageSource;

    
    public ExceptionHandlerConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestBody(HttpMessageNotReadableException ex, Locale locale)
    {
    	String message = messageSource.getMessage("error.requestbody.invalid",null, locale);
    	return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(message));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
    	Map<String, String> response = new HashMap<>();
        String localizedMessage = messageSource.getMessage(ex.getErrorCode(), null, LocaleContextHolder.getLocale());
        response.put("error", localizedMessage);
        return ResponseEntity.status(ex.getStatus()).body(response);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthenticationException(AuthenticationException ex)
    {
    	Map<String, String> response = new HashMap<>();
    	String localizedMessage = messageSource.getMessage("error.invalid.credentials",null,LocaleContextHolder.getLocale());
    	response.put("error", localizedMessage);
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
    	Map<String, String> errors = new HashMap<>();
        Locale locale = LocaleContextHolder.getLocale();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String messageKey = error.getDefaultMessage();

            // Lấy attributes từ ConstraintViolation (để có min, max, v.v.)
            Map<String, Object> attributes = null;
            try {
                var constraintViolation = error.unwrap(ConstraintViolation.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> attrMap = (Map<String, Object>) constraintViolation
                        .getConstraintDescriptor()
                        .getAttributes();

                attributes = attrMap;
            } catch (Exception ignored) {
            }

            //Lấy message đã dịch theo locale
            String localizedMessage;
            try {
                localizedMessage = messageSource.getMessage(messageKey, null, locale);
            } catch (NoSuchMessageException e) {
                localizedMessage = messageKey;
            }

            //Thay các biến {value} bằng giá trị thực
            if (attributes != null) {
                for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                    String placeholder = "{" + entry.getKey() + "}";
                    localizedMessage = localizedMessage.replace(placeholder, String.valueOf(entry.getValue()));
                }
            }

            errors.put(fieldName, localizedMessage);
        });

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
    	Map<String, String> response = new HashMap<>();
        String localizedMessage = messageSource.getMessage("error.general", null, "An unexpected error occurred: " + ex.getMessage(), LocaleContextHolder.getLocale());
        response.put("error", localizedMessage);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
