package com.vietbank.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vietbank.entity.BankAccount;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, String>{
	
	List<BankAccount> findByUserId(String userId);
    Optional<BankAccount> findByAccountNumber(String accountNumber);
    
    void deleteByUserId(String userId);
}
