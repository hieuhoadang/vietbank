package com.vietbank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vietbank.entity.BankAccount;
import com.vietbank.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
	
	List<Transaction> findByFromAccountIdOrToAccountId(BankAccount fromAccountId, BankAccount toAccountId);
}
