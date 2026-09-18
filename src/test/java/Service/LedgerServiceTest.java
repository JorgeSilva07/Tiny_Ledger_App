package Service;

import Model.Transaction;
import Model.TransactionType;
import Repository.AccountStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LedgerServiceTest {

    private LedgerService ledgerService;

    @BeforeEach
    void setUp() {
        ledgerService = new LedgerService(new AccountStore());
    }

    @Test
    void depositIncreasesBalanceAndCreatesAccountImplicitly() {
        BigDecimal balance = ledgerService.deposit("alice", new BigDecimal("100.00"));

        assertThat(balance).isEqualTo(new BigDecimal("100.00"));
        assertThat(ledgerService.getBalance("alice")).isEqualTo(new BigDecimal("100.00"));
    }

    @Test
    void withdrawDecreasesBalance() {
        ledgerService.deposit("alice", new BigDecimal("100.00"));

        BigDecimal balance = ledgerService.withdraw("alice", new BigDecimal("30.00"));

        assertThat(balance).isEqualTo(new BigDecimal("70.00"));
    }

    @Test
    void withdrawMoreThanBalanceThrowsBadRequest() {
        ledgerService.deposit("alice", new BigDecimal("10.00"));

        assertThatThrownBy(() -> ledgerService.withdraw("alice", new BigDecimal("50.00")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("insufficient funds");
    }

    @Test
    void withdrawFromUnknownAccountThrowsNotFound() {
        assertThatThrownBy(() -> ledgerService.withdraw("test", new BigDecimal("10.00")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void getBalanceOfUnknownAccountThrowsNotFound() {
        assertThatThrownBy(() -> ledgerService.getBalance("test"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void depositOfZeroOrNegativeAmountIsRejected() {
        assertThatThrownBy(() -> ledgerService.deposit("alice", BigDecimal.ZERO))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("greater than zero");

        assertThatThrownBy(() -> ledgerService.deposit("alice", new BigDecimal("-5.00")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    void amountWithMoreThanTwoDecimalPlacesIsRejected() {
        assertThatThrownBy(() -> ledgerService.deposit("alice", new BigDecimal("10.123")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("2 decimal digits");
    }

    @Test
    void transactionHistoryRecordsEachMovementInOrder() {
        ledgerService.deposit("alice", new BigDecimal("100.00"));
        ledgerService.withdraw("alice", new BigDecimal("40.00"));

        List<Transaction> history = ledgerService.getTransactionHistory("alice");

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getType()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(history.get(0).getAmount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(history.get(1).getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(history.get(1).getAmount()).isEqualTo(new BigDecimal("40.00"));
    }
}