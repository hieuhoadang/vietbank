package com.vietbank.config;

import java.util.Collections;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import com.vietbank.entity.User;
import com.vietbank.repository.UserRepository;

@Component
public class UserDetailService implements UserDetailsService {
	
	private final UserRepository userRepository;
	
	
	public UserDetailService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
		
		User user = userRepository.findByPhoneNumber(username)
				.orElseThrow(()-> new UsernameNotFoundException("{error.invalid.credentials}"));
		
		return new org.springframework.security.core.userdetails.User
				(
					user.getUsername(),
					user.getPasswordHash(),
					Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
				);
				
	}

}
