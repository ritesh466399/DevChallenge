package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AccountsServiceTest {

  @Autowired
  private AccountsService accountsService;

  @Test
  public void addAccount() throws Exception {
    Account account = new Account("Id-123");
    account.setBalance(new BigDecimal(1000));
    this.accountsService.createAccount(account);

    assertThat(this.accountsService.getAccount("Id-123")).isEqualTo(account);
  }

  @Test
  public void addAccount_failsOnDuplicateId() throws Exception {
    String uniqueId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueId);
    this.accountsService.createAccount(account);

    try {
      this.accountsService.createAccount(account);
      fail("Should have failed when adding duplicate account");
    } catch (DuplicateAccountIdException ex) {
      assertThat(ex.getMessage()).isEqualTo("Account id " + uniqueId + " already exists!");
    }

  }
  @Test
  public void accountbalanceCheck() throws Exception {
	  String uniqueAccountId = "Id-" + System.currentTimeMillis();
	    Account accountFrom = new Account(uniqueAccountId, new BigDecimal("233"));
	    this.accountsService.createAccount(accountFrom);
	    String uniqueAccountId1 = "Id-" + System.currentTimeMillis();
	    Account accountTo = new Account(uniqueAccountId1, new BigDecimal("999"));
	    this.accountsService.createAccount(accountTo);
	    Thread t1 =  new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i =0 ;i<10000 ;i++) {
					//random.nextInt(100);
					accountsService.transfer(accountFrom, accountTo, new BigDecimal(Math.random()));
					System.out.println("running" +Thread.currentThread().getName());
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	    });
	    
	  Thread t2=  new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i =0 ;i<10000 ;i++) {
					//random.nextInt(100);
					accountsService.transfer(accountTo, accountFrom, new BigDecimal(Math.random()));
					System.out.println("running" +Thread.currentThread().getName());
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
	    });
	    t1.start();
	    t2.start();
	    t1.join();
	    t2.join();
	    org.junit.Assert.assertTrue(new BigDecimal(1232).compareTo(this.accountsService.getAccount(uniqueAccountId).getBalance()
	    		.add(this.accountsService.getAccount(uniqueAccountId1).getBalance())) == 0);
  }

  


  
}


