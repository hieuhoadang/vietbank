package com.vietbank.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vietbank.entity.PasswordResetToken;
import com.vietbank.entity.User;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String>{

	boolean existsById(String id);
	
	Optional<PasswordResetToken> findByIdAndUser(String id, User user);
}
