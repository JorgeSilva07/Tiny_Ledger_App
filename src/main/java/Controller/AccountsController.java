package Controller;

import Service.LedgerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountsController {

    private final LedgerService ledgerService;

    public AccountsController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @GetMapping
    public List<BalanceResponse> getAllAccounts() {
        return ledgerService.getAllAccounts().stream()
                .map(account -> new BalanceResponse(account.getId(), account.getBalance()))
                .toList();
    }

    @GetMapping("/{accountId}/balance")
    public BalanceResponse getBalance(@PathVariable String accountId) {
        return new BalanceResponse(accountId, ledgerService.getBalance(accountId));
    }
}
