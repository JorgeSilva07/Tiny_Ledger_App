package Repository;

import Model.Account;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Repository
public class AccountStore {

    private final ConcurrentHashMap<String, Account> accounts = new ConcurrentHashMap<>();

    public Optional<Account> findById(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public Account getOrCreate(String accountId) {
        Account account = accounts.get(accountId);
        if (account != null) {
            return account;
        }

        Account newAccount = new Account(accountId);
        Account existing = accounts.putIfAbsent(accountId, newAccount);
        return existing != null ? existing : newAccount;    }

    public Collection<Account> findAll() {
        return accounts.values();
    }
}
