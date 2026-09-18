package Controller;

import Model.Transaction;
import Service.LedgerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/accounts/{accountId}")
public class TransactionController {

    private final LedgerService ledgerService;

    public TransactionController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/deposits")
    public BalanceResponse deposit(@PathVariable String accountId, @RequestBody AmountRequest request) {
        return new BalanceResponse(accountId, ledgerService.deposit(accountId, request.getAmount()));
    }

    @PostMapping("/withdrawals")
    public BalanceResponse withdraw(@PathVariable String accountId, @RequestBody AmountRequest request) {
        return new BalanceResponse(accountId, ledgerService.withdraw(accountId, request.getAmount()));
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(@PathVariable String accountId) {
        return ledgerService.getTransactionHistory(accountId);
    }
}
