package com.vietbank.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.vietbank.dto.UserDto;
import com.vietbank.entity.BankAccount;
import com.vietbank.entity.Transaction;
import com.vietbank.entity.User;
import com.vietbank.enums.AccountStatus;
import com.vietbank.enums.Role;
import com.vietbank.enums.TransactionStatus;
import com.vietbank.enums.TransactionType;
import com.vietbank.exception.CustomException;
import com.vietbank.repository.BankAccountRepository;
import com.vietbank.repository.TransactionRepository;
import com.vietbank.repository.UserRepository;


@Service
public class CustomerService {
	
	private final UserRepository userRepository;
	private final BankAccountRepository accountRepository;
	private final TransactionRepository transactionRepository;

	public CustomerService(UserRepository userRepository, BankAccountRepository accountRepository,
			TransactionRepository transactionRepository) {
		this.userRepository = userRepository;
		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
	}

	public UserDto getProfile() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
        if (!user.getRole().equals(Role.CUSTOMER)) {
            throw new CustomException("error.user.notCustomer", HttpStatus.BAD_REQUEST);
        }
        return mapToUserDto(user);
    }

    public UserDto updateProfile(String fullName, String email, LocalDate dateOfBirth, String address) {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
        user.setFullName(fullName);
        user.setEmail(email);
        user.setDateOfBirth(LocalDate.from(dateOfBirth));
        user.setAddress(address);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return mapToUserDto(user);
    }

    public List<BankAccount> getAccounts() {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
        return accountRepository.findByUserId(user.getId());
    }

    public BankAccount getAccountById(String accountId) {
        String phoneNumber = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException("error.account.notFound", HttpStatus.NOT_FOUND));
        if (!account.getUser().getId().equals(user.getId())) {
            throw new CustomException("error.account.invalidUser", HttpStatus.BAD_REQUEST);
        }
        return account;
    }

    public Transaction transfer(String fromAccountId, String toAccountId, BigDecimal amount, String description) {
    	String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Authenticated userId: " + userId);
        BankAccount fromAccount = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new CustomException("error.account.notFound", HttpStatus.NOT_FOUND));
        System.out.println("fromAccount userId: " + fromAccount.getUser().getId());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("error.amount.invalid", HttpStatus.BAD_REQUEST);
        }
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new CustomException("error.account.inactive", HttpStatus.BAD_REQUEST);
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new CustomException("error.balance.insufficient", HttpStatus.BAD_REQUEST);
        }
        BankAccount toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new CustomException("error.account.notFound", HttpStatus.NOT_FOUND));
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new CustomException("error.account.inactive", HttpStatus.BAD_REQUEST);
        }
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);
        
        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccount);
        transaction.setToAccountId(toAccount);
        transaction.setAmount(amount);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setCreatedAt(LocalDateTime.now());
        
        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactions(String accountId) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException("error.account.notFound", HttpStatus.NOT_FOUND));
        if (!account.getUser().getId().equals(userId)) {
            throw new CustomException("error.account.invalidUser", HttpStatus.BAD_REQUEST);
        }
        return transactionRepository.findByFromAccountIdOrToAccountId(account, account);
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
