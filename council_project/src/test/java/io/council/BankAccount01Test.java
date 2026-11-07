package io.council;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class BankAccount01Test {

    private BankAccount01 account;

    @BeforeEach
    void setUp() {
        account = new BankAccount01("ACC123", "John Doe", BankAccount01.AccountType.SAVINGS, 1000.0);
    }

    @Test
    @DisplayName("Constructor should initialize fields correctly")
    void constructorInitialization() {
        assertEquals("ACC123", account.getAccountId());
        assertEquals("John Doe", account.getOwnerName());
        assertEquals(BankAccount01.AccountType.SAVINGS, account.getType());
        assertEquals(1000.0, account.getBalance());
        assertFalse(account.isFrozen());
        assertEquals(1, account.getTransactionHistory().size());
        assertEquals("OPEN:1000.0", account.getTransactionHistory().get(0));
    }

    @Test
    @DisplayName("Constructor should throw exception for invalid arguments")
    void constructorValidation() {
        assertThrows(IllegalArgumentException.class, () -> new BankAccount01(null, "Name", BankAccount01.AccountType.CHECKING, 100));
        assertThrows(IllegalArgumentException.class, () -> new BankAccount01("ID", "", BankAccount01.AccountType.CHECKING, 100));
        assertThrows(IllegalArgumentException.class, () -> new BankAccount01("ID", "Name", BankAccount01.AccountType.CHECKING, -1.0));
    }

    @Test
    @DisplayName("Deposit should increase balance and update history")
    void depositSuccess() {
        account.deposit(500.0);
        assertEquals(1500.0, account.getBalance());
        assertEquals("DEPOSIT:500.0", account.getTransactionHistory().get(1));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -10.0})
    @DisplayName("Deposit should throw exception for non-positive amounts")
    void depositInvalid(double amount) {
        assertThrows(IllegalArgumentException.class, () -> account.deposit(amount));
    }

    @Test
    @DisplayName("Withdraw should decrease balance and update history")
    void withdrawSuccess() {
        account.withdraw(400.0);
        assertEquals(600.0, account.getBalance());
        assertEquals("WITHDRAW:400.0", account.getTransactionHistory().get(1));
    }

    @Test
    @DisplayName("Withdraw should throw exception for insufficient funds or invalid amount")
    void withdrawInvalid() {
        assertThrows(IllegalStateException.class, () -> account.withdraw(2000.0));
        assertThrows(IllegalArgumentException.class, () -> account.withdraw(-50.0));
    }

    @Test
    @DisplayName("Transfer should move funds between accounts")
    void transferSuccess() {
        BankAccount01 target = new BankAccount01("ACC456", "Jane Doe", BankAccount01.AccountType.CHECKING, 500.0);
        account.transfer(target, 300.0);
        assertEquals(700.0, account.getBalance());
        assertEquals(800.0, target.getBalance());
        assertEquals("TRANSFER_OUT:300.0->ACC456", account.getTransactionHistory().get(1));
        assertEquals("TRANSFER_IN:300.0<-ACC123", target.getTransactionHistory().get(1));
    }

    @Test
    @DisplayName("Transfer should fail if frozen or target invalid")
    void transferFailure() {
        BankAccount01 target = new BankAccount01("ACC456", "Jane Doe", BankAccount01.AccountType.CHECKING, 500.0);
        assertThrows(IllegalArgumentException.class, () -> account.transfer(null, 100));
        assertThrows(IllegalArgumentException.class, () -> account.transfer(account, 100));
        
        account.freeze();
        assertThrows(IllegalStateException.class, () -> account.transfer(target, 100));
    }

    @Test
    @DisplayName("Frozen status should restrict transactions")
    void freezeRestrict() {
        account.freeze();
        assertTrue(account.isFrozen());
        assertThrows(IllegalStateException.class, () -> account.deposit(100));
        assertThrows(IllegalStateException.class, () -> account.withdraw(100));
        
        account.unfreeze();
        assertFalse(account.isFrozen());
        assertDoesNotThrow(() -> account.deposit(100));
    }

    @Test
    @DisplayName("Interest calculation should be correct based on type and rate")
    void calculateInterest() {
        // Savings: 1000 * (5/100) * (6/12) = 1000 * 0.05 * 0.5 = 25.0
        assertEquals(25.0, account.calculateInterest(5.0, 6));
        
        BankAccount01 checking = new BankAccount01("C1", "Owner", BankAccount01.AccountType.CHECKING, 1000.0);
        assertEquals(0.0, checking.calculateInterest(5.0, 6));
    }

    @Test
    @DisplayName("Interest calculation validation")
    void calculateInterestInvalid() {
        assertThrows(IllegalArgumentException.class, () -> account.calculateInterest(-1, 6));
        assertThrows(IllegalArgumentException.class, () -> account.calculateInterest(5, 0));
    }
}