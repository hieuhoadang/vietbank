package com.vietbank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.vietbank.entity.User;
import com.vietbank.enums.Role;
import java.time.LocalDateTime;


@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

	Optional<User> findByPhoneNumber(String phoneNumber);
	
	Optional<User> findByEmail(String email);
	Optional<User> findByIdCard(String idCard);

    Optional<User> findByIdAndRole(String id, Role role);
    
    
    Page<User> findAllByRole(
        Role role,
        Pageable pageable
    );
    
    List<User> findByDeletionScheduleAt(LocalDateTime hold);
}
