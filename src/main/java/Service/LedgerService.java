package Service;

import Model.Account;
import Model.Transaction;
import Repository.AccountStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

@Service
public class LedgerService {

    private final AccountStore accountStore;

    public LedgerService(AccountStore accountStore) {
        this.accountStore = accountStore;
    }

    public BigDecimal deposit(String accountId, BigDecimal amount) {
        validateAmount(amount);
        Account account = accountStore.getOrCreate(accountId);
        account.deposit(amount);
        return account.getBalance();
    }

    public BigDecimal withdraw(String accountId, BigDecimal amount) {
        validateAmount(amount);
        Account account = getExistingAccount(accountId);

        if (amount.compareTo(account.getBalance()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Account '" + accountId + "' has insufficient funds for this withdrawal");
        }

        account.withdraw(amount);
        return account.getBalance();
    }

    public BigDecimal getBalance(String accountId) {
        return getExistingAccount(accountId).getBalance();
    }

    public List<Transaction> getTransactionHistory(String accountId) {
        return getExistingAccount(accountId).getTransactions();
    }

    public Collection<Account> getAllAccounts() {
        return accountStore.findAll();
    }

    private Account getExistingAccount(String accountId) {
        return accountStore.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Account '" + accountId + "' not found"));
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }
        if (amount.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must have at most 2 decimal digits");
        }
    }
}
