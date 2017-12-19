package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientBalanceExcepion;
import com.db.awmd.challenge.exception.NegativeAmountExcepion;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountsService {

	@Getter
	private final AccountsRepository accountsRepository;
	
	@Getter
	private final NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository,NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService =  notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	/*This method can safely acquire any number of locks in any order without causing deadlock, 
	 * using the tryLock() method of ReentrantLock. */
	private void acquireLocks(Lock firstLock, Lock secondLock) throws InterruptedException {
		while (true) {
			// Acquire locks

			boolean gotFirstLock = false;
			boolean gotSecondLock = false;

			try {
				gotFirstLock = firstLock.tryLock();
				gotSecondLock = secondLock.tryLock();
			} finally {
				if (gotFirstLock && gotSecondLock) {
					return;
				}

				if (gotFirstLock) {
					firstLock.unlock();
				}

				if (gotSecondLock) {
					secondLock.unlock();
				}
			}

			// Locks not acquired
			Thread.sleep(1);
		}
	}

	public void transfer(Account accountFrom, Account accountTo, BigDecimal amount) {
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			 throw new NegativeAmountExcepion("negative amount transfer not possible");
		}
		 if(accountFrom.getBalance().compareTo(amount) == -1 )  {
			  throw new InsufficientBalanceExcepion("insufficient balance");
		  }
		Lock lock1 = new ReentrantLock();
		Lock lock2 = new ReentrantLock();
		try {
			acquireLocks(lock1, lock2);
			this.accountsRepository.transfer(accountFrom, accountTo, amount);
			notificationService.notifyAboutTransfer(accountFrom, "$ "+ amount + "has been debited to  " + accountTo );
			notificationService.notifyAboutTransfer(accountTo, "$ "+ amount + "has been Credited from " + accountFrom );
		} catch (InterruptedException e) {
			log.error(e.getMessage());
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} finally {
			lock1.unlock();
			lock2.unlock();
		}

	}

}