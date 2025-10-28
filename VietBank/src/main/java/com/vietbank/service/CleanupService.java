package com.vietbank.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vietbank.repository.BankAccountRepository;
import com.vietbank.repository.UserRepository;

@Service
public class CleanupService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);
	
	private static final long DELETION_DELAY_DAYS = 30;
	
	private final UserRepository userRepository;
	private final BankAccountRepository bankAccountRepository;
	
	
	public CleanupService(UserRepository userRepository, BankAccountRepository bankAccountRepository) {
		this.userRepository = userRepository;
		this.bankAccountRepository = bankAccountRepository;
	}
	
	@Scheduled(cron = "0 0 0 * * ?")// chạy lúc 00h
	@Transactional
	public void cleanupInactiveUsers()
	{
		LocalDateTime hold = LocalDateTime.now().minusDays(DELETION_DELAY_DAYS);
		userRepository.findByDeletionScheduleAt(hold)
				.forEach(user -> {
					LOGGER.info("Deleting user: id={}, phoneNumber={}", user.getId(), user.getPhoneNumber());
					bankAccountRepository.findByUserId(user.getId());
					userRepository.delete(user);
				});
	}
	
}
