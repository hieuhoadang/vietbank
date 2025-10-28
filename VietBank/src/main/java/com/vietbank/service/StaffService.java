package com.vietbank.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vietbank.dto.BankAccountDto;
import com.vietbank.dto.UserDto;
import com.vietbank.dto.request.CreateBankAccountRequest;
import com.vietbank.dto.request.CreateUserRequest;
import com.vietbank.dto.request.UpdateBankAccountRequest;
import com.vietbank.entity.BankAccount;
import com.vietbank.entity.User;
import com.vietbank.enums.AccountStatus;
import com.vietbank.enums.Role;
import com.vietbank.exception.CustomException;
import com.vietbank.repository.BankAccountRepository;
import com.vietbank.repository.UserRepository;
import com.vietbank.specification.CustomerSearchSpecification;

@Service
public class StaffService {

	private final UserRepository userRepository;
	private final BankAccountRepository accountRepository;
	private PasswordEncoder passwordEncoder;
	
	public StaffService(UserRepository userRepository, BankAccountRepository bankAccountRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.accountRepository = bankAccountRepository;
		this.passwordEncoder = passwordEncoder;
	}
	
	@Transactional
	public UserDto createCustomer(CreateUserRequest request) {
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new CustomException("error.phone.exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException("error.email.exists", HttpStatus.BAD_REQUEST);
        }
        if (userRepository.findByIdCard(request.getIdCard()).isPresent()) {
            throw new CustomException("error.idCard.exists", HttpStatus.BAD_REQUEST);
        }
        var user = new User();
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIdCard(request.getIdCard());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        return mapToUserDto(user);
    }

    public UserDto updateCustomer(String customerId, CreateUserRequest request) {
        var user = userRepository.findById(customerId)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
        
        if (!user.getRole().equals(Role.CUSTOMER)) {
            throw new CustomException("error.user.notCustomer", HttpStatus.BAD_REQUEST);
        }
        
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent() && !user.getPhoneNumber().equals(request.getPhoneNumber())) {
            throw new CustomException("error.phone.exists", HttpStatus.BAD_REQUEST);
        }
        
        if (userRepository.findByEmail(request.getEmail()).isPresent() && !user.getEmail().equals(request.getEmail())) {
            throw new CustomException("error.email.exists", HttpStatus.BAD_REQUEST);
        }
        
        if (userRepository.findByIdCard(request.getIdCard()).isPresent() && !user.getIdCard().equals(request.getIdCard())) {
            throw new CustomException("error.idCard.exists", HttpStatus.BAD_REQUEST);
        }
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return mapToUserDto(user);
    }
    
    @Transactional
    public void deleteCustomer(String customerId) {
        var user = userRepository.findById(customerId)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
        if (!user.getRole().equals(Role.CUSTOMER)) {
            throw new CustomException("error.user.notCustomer", HttpStatus.BAD_REQUEST);
        }
        
        if(user.getDeletionScheduleAt() != null)
        {
        	throw new CustomException("error.user.notCustomer", HttpStatus.BAD_REQUEST);
        }
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletionScheduleAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public UserDto getCustomer(String customerId) {
        var user = userRepository.findById(customerId)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
        if (!(user.getRole().equals(Role.CUSTOMER))) {
            throw new CustomException("error.user.notCustomer", HttpStatus.BAD_REQUEST);
        }
        return mapToUserDto(user);
    }

    public Page<UserDto> getCustomers(String name, String phone, String idCard, int page, int size) {
        return userRepository.findAll(
        		CustomerSearchSpecification.searchCustomers(name, phone, idCard, Role.CUSTOMER),
                PageRequest.of(page, size)
        ).map(this::mapToUserDto);
    }
    
    @Transactional
    public BankAccountDto createAccount(String customerId, CreateBankAccountRequest request) {
        var user = userRepository.findById(customerId)
                .orElseThrow(() -> new CustomException("error.user.notFound", HttpStatus.NOT_FOUND));
        if (!user.getRole().equals(Role.CUSTOMER)) {
            throw new CustomException("error.user.notCustomer", HttpStatus.BAD_REQUEST);
        }
        
        String accountNumber = request.getAccountNumber();
        
        if(accountNumber == null || accountNumber.isBlank())
        {
        	accountNumber = generateUniqueAccountNumber();
        }else {
        	
        	if (accountRepository.findByAccountNumber(request.getAccountNumber()).isPresent()) {
                throw new CustomException("error.accountNumber.exists", HttpStatus.BAD_REQUEST);
            }
		}
        
        BankAccount account = new BankAccount();
        account.setId(UUID.randomUUID().toString());
        account.setAccountNumber(accountNumber);
        account.setAccountType(request.getAccountType());
        account.setBalance(BigDecimal.ZERO);
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return mapToBankAccountDto(account);
    }

    public BankAccountDto updateAccount(String customerId, String accountId, UpdateBankAccountRequest request) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException("error.account.notFound", HttpStatus.NOT_FOUND));
        if (!account.getUser().getId().equals(customerId)) {
            throw new CustomException("error.account.invalidUser", HttpStatus.BAD_REQUEST);
        }
        if (accountRepository.findByAccountNumber(request.getAccountNumber()).filter(a -> !a.getId().equals(accountId)).isPresent()) {
            throw new CustomException("error.accountNumber.exists", HttpStatus.BAD_REQUEST);
        }
        account.setAccountNumber(request.getAccountNumber());
        account.setAccountType(request.getAccountType());
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return mapToBankAccountDto(account);
    }

    public BankAccountDto deposit(String customerId, String accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CustomException("error.amount.invalid", HttpStatus.BAD_REQUEST);
        }
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException("error.account.notFound", HttpStatus.NOT_FOUND));
        if (!account.getUser().getId().equals(customerId)) {
            throw new CustomException("error.account.invalidUser", HttpStatus.BAD_REQUEST);
        }
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new CustomException("error.account.inactive", HttpStatus.BAD_REQUEST);
        }
        account.setBalance(account.getBalance().add(amount));
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return mapToBankAccountDto(account);
    }

    public BankAccountDto deactivateAccount(String customerId, String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException("error.account.notFound", HttpStatus.NOT_FOUND));
        if (!account.getUser().getId().equals(customerId)) {
            throw new CustomException("error.account.invalidUser", HttpStatus.BAD_REQUEST);
        }
        account.setStatus(AccountStatus.INACTIVE);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return mapToBankAccountDto(account);
    }
    
    public BankAccountDto activateAccount(String customerId, String accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException("error.account.notFound", HttpStatus.NOT_FOUND));
        if (!account.getUser().getId().equals(customerId)) {
            throw new CustomException("error.account.invalidUser", HttpStatus.BAD_REQUEST);
        }
        account.setStatus(AccountStatus.ACTIVE);
        account.setUpdatedAt(LocalDateTime.now());
        accountRepository.save(account);
        return mapToBankAccountDto(account);
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
    
    private BankAccountDto mapToBankAccountDto(BankAccount account)
    {
    	BankAccountDto dto = new BankAccountDto();
    	dto.setId(account.getId());
    	dto.setAccountNumber(account.getAccountNumber());
    	dto.setAccountType(account.getAccountType());
    	dto.setBalance(account.getBalance());
    	dto.setUserId(account.getUser().getId());
    	dto.setActive(account.getStatus());
    	return dto;
    }
    
    // sinh số tài khoản ngẫu nhiên
    private String generateUniqueAccountNumber()
    {
    	String accountNumber;
    	do {
			accountNumber = generateRandomAccountNumber();
		} while (accountRepository.findByAccountNumber(accountNumber).isPresent());
    	return accountNumber;
    }
    
    private String generateRandomAccountNumber()
    {
    	int length = 13;
    	String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    	String digits = "0123456789";
    	Random random = new Random();
    	StringBuilder sb = new StringBuilder();
    	// 3 ký tự đầu là chữ cái 
    	for(int i = 0;i<3;i++)
    	{
    		int index = random.nextInt(letters.length());
    		sb.append(letters.charAt(index));
    	}
    	// các ký tự còn lại là chữ số
    	for(int i = 0;i<length;i++)
    	{
    		int index = random.nextInt(digits.length());
    		sb.append(digits.charAt(index));
    	}
    	return sb.toString();
    }
}
