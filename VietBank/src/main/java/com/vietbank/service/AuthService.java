package com.vietbank.service;


import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.vietbank.dto.UserDto;
import com.vietbank.dto.request.CreateUserRequest;
import com.vietbank.dto.request.ForgotPasswordRequest;
import com.vietbank.dto.request.IntrospectRequest;
import com.vietbank.dto.request.LoginRequest;
import com.vietbank.dto.request.LogoutRequest;
import com.vietbank.dto.request.ResetPasswordRequest;
import com.vietbank.dto.response.IntrospectResponse;
import com.vietbank.entity.InvalidatedToken;
import com.vietbank.entity.PasswordResetToken;
import com.vietbank.entity.User;
import com.vietbank.enums.Role;
import com.vietbank.exception.CustomException;
import com.vietbank.repository.InvalidatedTokenRepository;
import com.vietbank.repository.PasswordResetTokenRepository;
import com.vietbank.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final InvalidatedTokenRepository invalidatedTokenRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Value("${jwt.signer-key}")
	private String SIGNER_KEY;
	
	@Value("${jwt.valid-duration}")
	private long VALID_DURATION;
	
	@Value("${jwt.refreshable-duration}")
	private long REFESHABLE_DURATION;
	@Value("${jwt.reset-token-duration}")
	private long RESET_TOKEN_DURATION;
	
	public IntrospectResponse introspect (IntrospectRequest request) throws JOSEException, ParseException
	{
		var token = request.getToken();
		boolean isValid = true;
		
		try {
			verifyToken(token, false);
		} catch (Exception e) {
			isValid = false;
		}
		return IntrospectResponse.builder().valid(isValid).build();
	}
	
	public String login(LoginRequest request) {
		try {
			Authentication authentication = authenticationManager.authenticate
	        		(
	        		new UsernamePasswordAuthenticationToken(request.getPhoneNumber(), request.getPassword())		
	        		);
	        SecurityContextHolder.getContext().setAuthentication(authentication);
	        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
	        		.orElseThrow(()-> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
	        return generateToken(user);
		} catch (BadCredentialsException e) {
			throw new CustomException("error.invalid.credentials", HttpStatus.UNAUTHORIZED);
		}
        
    }
	
	@Transactional
    public UserDto registerStaff(CreateUserRequest request) {
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new CustomException("error.phone.exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("error.email.exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByIdCard(request.getIdCard()).isPresent()) {
            throw new CustomException("error.idCard.exists", HttpStatus.BAD_REQUEST);
        }
        var user = mapCreateRequestToUser(request);
        user.setRole(Role.STAFF);
        userRepository.save(user);
        return mapToUserDto(user);
    }
	
	@Transactional
	public String forgotPassword(ForgotPasswordRequest request)
	{
		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(()-> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
		// Generate jti for both JWT and PasswordResetToken
		String jti = UUID.randomUUID().toString();
		// Pass jti to generateResetToken
		String token = generateResetToken(user,jti);
		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setId(jti);
		resetToken.setUser(user);
		resetToken.setExpiryTime(LocalDateTime.now().plusSeconds(RESET_TOKEN_DURATION));
		
		passwordResetTokenRepository.saveAndFlush(resetToken);
		LOGGER.info("Password reset token generated for user: {}, token: {}", user.getPhoneNumber());
		return token;
	}
	
	@Transactional
	public void resetPassword(ResetPasswordRequest request) throws JOSEException, ParseException
	{
		try {
            SignedJWT signedJWT = verifyResetToken(request.getToken());
            String userId = signedJWT.getJWTClaimsSet().getSubject();
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
            PasswordResetToken resetToken = passwordResetTokenRepository.findByIdAndUser(signedJWT.getJWTClaimsSet().getJWTID(), user)
                    .orElseThrow(() -> new CustomException("error.reset.token.invalid", HttpStatus.BAD_REQUEST));
            
            if (resetToken.getExpiryTime().isBefore(LocalDateTime.now())) {
                passwordResetTokenRepository.delete(resetToken);
                throw new CustomException("error.reset.token.expired", HttpStatus.BAD_REQUEST);
            }
            
            user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            
            userRepository.save(user);
            // xóa token ngay sau khi thiết lập lại mật khẩu thành công
            passwordResetTokenRepository.delete(resetToken);
            // Cho token trên vào danh sách token hết hạn để tránh sử dụng lại
            InvalidatedToken invalidatedToken = new InvalidatedToken();
            invalidatedToken.setId(signedJWT.getJWTClaimsSet().getJWTID());
            invalidatedToken.setExpriyTime(LocalDateTime.ofInstant(signedJWT.getJWTClaimsSet().getExpirationTime().toInstant(), ZoneId.systemDefault()));
            invalidatedTokenRepository.saveAndFlush(invalidatedToken);
            
        } catch (JOSEException | ParseException e) {
            throw new CustomException("error.reset.token.invalid", HttpStatus.BAD_REQUEST);
        }
	}
	
	public void logout(LogoutRequest request) throws JOSEException, ParseException
	{
		try {
			var signedToken = verifyToken(request.getToken(), true);
			String jti = signedToken.getJWTClaimsSet().getJWTID();
			Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
			
			InvalidatedToken invalidatedToken = new InvalidatedToken();
			invalidatedToken.setId(jti);
			invalidatedToken.setExpriyTime(LocalDateTime.ofInstant(expiryTime.toInstant(), ZoneId.systemDefault()));
			
			
			invalidatedTokenRepository.saveAndFlush(invalidatedToken);
		} catch (Exception e) {
			// TODO: handle exception
			log.info("Token already expired");
		}
		
	}
	
	public String generateToken(User user)
	{
		JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
		
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
				.subject(user.getUsername())
				.issuer("vietbank")
				.issueTime(new Date())
				.expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
				.jwtID(UUID.randomUUID().toString())
				.claim("scope", buildScope(user))
				.build();
		
		Payload payload = new Payload(jwtClaimsSet.toJSONObject());
		
		JWSObject jwsObject = new JWSObject(header, payload);
		
		try {
			jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
			return jwsObject.serialize();
		} catch (Exception e) {
			// TODO: handle exception
			LOGGER.error("Cannot create token for user: {}", user.getUsername(), e);
			throw new CustomException("error.token.generation", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public String generateResetToken(User user, String jti)
	{
		JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
		
		JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
				.subject(user.getId())
				.issuer("vietbank")
				.issueTime(new Date())
				.expirationTime(new Date(Instant.now().plus(RESET_TOKEN_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
				.jwtID(jti)
				.claim("type", "reset")
				.build();
		
		Payload payload = new Payload(jwtClaimsSet.toJSONObject());
		
		JWSObject jwsObject = new JWSObject(header, payload);
		
		try {
			jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
			return jwsObject.serialize();
		} catch (Exception e) {
			LOGGER.error("Cannot create token for user: {}", user.getPhoneNumber(), e);
			throw new CustomException("error.token.generation", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	public SignedJWT verifyToken (String token, boolean isRefresh) throws JOSEException, ParseException
	{
		JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
		
		SignedJWT signedJWT = SignedJWT.parse(token);
		
		Date expiryTime = isRefresh ? new Date(signedJWT.getJWTClaimsSet().getIssueTime()
												.toInstant().plus(REFESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli()
				) : signedJWT.getJWTClaimsSet().getExpirationTime();
		
		if(!signedJWT.verify(verifier) || !expiryTime.after(new Date()))
		{
			throw new CustomException("error.token.invalidated", HttpStatus.UNAUTHORIZED);
		}
		
		if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
		{
			throw new CustomException("error.token.invalidated", HttpStatus.UNAUTHORIZED);
		}
		return signedJWT;
		
	}
	
	public SignedJWT verifyResetToken (String token) throws JOSEException, ParseException
	{
		JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);
        

        if (!signedJWT.verify(verifier)) {
            throw new CustomException("error.reset.token.invalid", HttpStatus.BAD_REQUEST);
        }

        if (!signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date())) {
            throw new CustomException("error.reset.token.invalid", HttpStatus.BAD_REQUEST);
        }

        String type = signedJWT.getJWTClaimsSet().getStringClaim("type");
        if (!"reset".equals(type)) {
            throw new CustomException("error.reset.token.invalid", HttpStatus.BAD_REQUEST);
        }

        String jti = signedJWT.getJWTClaimsSet().getJWTID();
        String userId = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));

        if (!passwordResetTokenRepository.findByIdAndUser(jti, user).isPresent()) {
            throw new CustomException("error.reset.token.invalid", HttpStatus.BAD_REQUEST);
        }
        LOGGER.info("Reset token verified successfully: jti={}, user_id={}", jti, userId);
        return signedJWT;
		
	}
    
    private String buildScope(User user) {
		// TODO Auto-generated method stub
		return "ROLE_" + user.getRole().name();
	}

	private User mapCreateRequestToUser(CreateUserRequest request) {
        var user = new User();
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIdCard(request.getIdCard());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        return user;
    }
    
    private UserDto mapToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setRole(user.getRole());
        dto.setFullName(user.getFullName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setIdCard(user.getIdCard());
        dto.setEmail(user.getEmail());
        dto.setDateOfBirth(user.getDateOfBirth());
        dto.setAddress(user.getAddress());
        dto.setActive(user.isActive());
        return dto;
    }
    
    
}
