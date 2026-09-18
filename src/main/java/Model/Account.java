package Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {

    private final String id;

    private BigDecimal balance;

    private final List<Transaction> transactions = new ArrayList<>();

    public Account(String id) {
        this.id = id;
        this.balance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public String getId() {
        return id;
    }

    public synchronized BigDecimal getBalance() {
        return balance;
    }

    public synchronized void deposit(BigDecimal amount) {
        balance = balance.add(amount).setScale(2, RoundingMode.HALF_UP);
        transactions.add(new Transaction(TransactionType.DEPOSIT, amount));
    }

    public synchronized void withdraw(BigDecimal amount) {
        balance = balance.subtract(amount).setScale(2, RoundingMode.HALF_UP);
        transactions.add(new Transaction(TransactionType.WITHDRAWAL, amount));
    }

    public synchronized List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }
}
