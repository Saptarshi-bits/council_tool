package io.council;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BankAccount01 {

    public enum AccountType { CHECKING, SAVINGS }

    private final String accountId;
    private final String ownerName;
    private final AccountType type;
    private double balance;
    private boolean frozen;
    private final List<String> transactionHistory;

    public BankAccount01(String accountId, String ownerName, AccountType type, double initialBalance) {
        if (accountId == null || accountId.isBlank()) throw new IllegalArgumentException("Account ID required");
        if (ownerName == null || ownerName.isBlank()) throw new IllegalArgumentException("Owner name required");
        if (initialBalance < 0) throw new IllegalArgumentException("Initial balance cannot be negative");
        this.accountId = accountId;
        this.ownerName = ownerName;
        this.type = type;
        this.balance = initialBalance;
        this.frozen = false;
        this.transactionHistory = new ArrayList<>();
        transactionHistory.add("OPEN:" + initialBalance);
    }

    public void deposit(double amount) {
        if (frozen) throw new IllegalStateException("Account is frozen");
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive");
        balance += amount;
        transactionHistory.add("DEPOSIT:" + amount);
    }

    public void withdraw(double amount) {
        if (frozen) throw new IllegalStateException("Account is frozen");
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive");
        if (amount > balance) throw new IllegalStateException("Insufficient funds");
        balance -= amount;
        transactionHistory.add("WITHDRAW:" + amount);
    }

    public void transfer(BankAccount01 target, double amount) {
        if (target == null) throw new IllegalArgumentException("Target account required");
        if (this == target) throw new IllegalArgumentException("Cannot transfer to same account");
        this.withdraw(amount);
        target.deposit(amount);
        transactionHistory.add("TRANSFER_OUT:" + amount + "->"+target.accountId);
        target.transactionHistory.add("TRANSFER_IN:" + amount + "<-"+this.accountId);
    }

    public void freeze() { this.frozen = true; }
    public void unfreeze() { this.frozen = false; }

    public double getBalance() { return balance; }
    public String getAccountId() { return accountId; }
    public String getOwnerName() { return ownerName; }
    public AccountType getType() { return type; }
    public boolean isFrozen() { return frozen; }
    public List<String> getTransactionHistory() { return Collections.unmodifiableList(transactionHistory); }

    public double calculateInterest(double annualRate, int months) {
        if (annualRate < 0) throw new IllegalArgumentException("Rate cannot be negative");
        if (months <= 0) throw new IllegalArgumentException("Months must be positive");
        if (type == AccountType.CHECKING) return 0;
        return balance * (annualRate / 100) * (months / 12.0);
    }

    @Override
    public String toString() {
        return "BankAccount01[" + accountId + ", " + ownerName + ", " + type + ", balance=" + balance + "]";
    }
}
