package com.vietbank.controller;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nimbusds.jose.JOSEException;
import com.vietbank.dto.ResponseSuccess;
import com.vietbank.dto.UserDto;
import com.vietbank.dto.request.CreateUserRequest;
import com.vietbank.dto.request.ForgotPasswordRequest;
import com.vietbank.dto.request.IntrospectRequest;
import com.vietbank.dto.request.LoginRequest;
import com.vietbank.dto.request.LogoutRequest;
import com.vietbank.dto.request.ResetPasswordRequest;
import com.vietbank.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
	
	private final AuthService authService;
	private final MessageSource messageSource;

	
	@PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        String token = authService.login(request);
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
		return ResponseEntity.ok(ResponseSuccess.create("success.login", result, messageSource));
    }

    @PostMapping("/register-staff")
    public ResponseEntity<ResponseSuccess<UserDto>> registerStaff(@Valid @RequestBody CreateUserRequest request) {
       UserDto userDto = authService.registerStaff(request);
       ResponseSuccess<UserDto> response = ResponseSuccess.create("success.register", userDto, messageSource);
       return ResponseEntity.ok(response);
    }
    
   @PostMapping("/introspect")
   public ResponseEntity<?> authenticateToken(@Valid @RequestBody IntrospectRequest request) throws JOSEException, ParseException
   {
	   var result = authService.introspect(request);
	   
	   if(!result.isValid())
	   {
		   return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
	   }
	   return ResponseEntity.ok(ResponseSuccess.create("success.introspect", result, messageSource));
   }
   
   @PostMapping("/logout")
   public ResponseEntity<?> logout (@Valid @RequestBody LogoutRequest request) throws JOSEException, ParseException
   {
	   authService.logout(request);
	return ResponseEntity.ok(ResponseSuccess.create("success.logout", null, messageSource));
	   
   }
   
   @PostMapping("/forgotPassword")
   public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request)
   {
	   String token = authService.forgotPassword(request);
	   Map<String, String> response = new HashMap<>();
	   response.put("token", token);
	   return ResponseEntity.ok(ResponseSuccess.create("success.forgot.password", response, messageSource));
   }
   
   @PostMapping("/reset-password")
   public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) throws JOSEException, ParseException
   {
	   authService.resetPassword(request);
	   return ResponseEntity.ok(ResponseSuccess.create("success.reset.password", null, messageSource));
   }
}
