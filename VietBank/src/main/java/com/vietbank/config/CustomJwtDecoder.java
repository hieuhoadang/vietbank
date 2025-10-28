package com.vietbank.config;

import java.text.ParseException;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.nimbusds.jose.JOSEException;
import com.vietbank.dto.request.IntrospectRequest;
import com.vietbank.service.AuthService;


@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder{

	private final AuthService authService;
	
	@Value("${jwt.signer-key}")
	private String SINGER_KEY;
    // giải mã token
	private NimbusJwtDecoder nimbusJwtDecoder = null;

	@Override
	public Jwt decode(String token) throws JwtException {
		
		try {
			var response = authService.introspect(IntrospectRequest.builder().token(token).build());
			
			if(!response.isValid())
				throw new JwtException("Token invalid");
		}catch (JOSEException | ParseException e) {
            throw new JwtException(e.getMessage());
        }
		
			if(Objects.isNull(nimbusJwtDecoder))
			{
				SecretKeySpec secretKeySpec = new SecretKeySpec(SINGER_KEY.getBytes(), "HS512");
				nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
						.macAlgorithm(MacAlgorithm.HS512)
						.build();
			}
		
		return nimbusJwtDecoder.decode(token);
	}

}
