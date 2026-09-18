package Controller;

import LedgerApp.LedgerApplication;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = LedgerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LedgerApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void depositThenWithdrawThenViewBalanceAndHistory() {
        ResponseEntity<BalanceResponse> deposit = deposit("jorge", "100.00");
        assertThat(deposit.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertNotNull(deposit.getBody());
        assertThat(deposit.getBody().getBalance()).isEqualTo(new BigDecimal("100.00"));

        ResponseEntity<BalanceResponse> withdraw = withdraw("jorge", "30.00");
        Assertions.assertNotNull(withdraw.getBody());
        assertThat(withdraw.getBody().getBalance()).isEqualTo(new BigDecimal("70.00"));

        ResponseEntity<BalanceResponse> balance =
                restTemplate.getForEntity("/accounts/jorge/balance", BalanceResponse.class);
        Assertions.assertNotNull(balance.getBody());
        assertThat(balance.getBody().getBalance()).isEqualTo(new BigDecimal("70.00"));

        ResponseEntity<String> history = restTemplate.getForEntity("/accounts/jorge/transactions", String.class);
        String historyBody = history.getBody();
        assertThat(historyBody).contains("\"type\":\"DEPOSIT\"").contains("\"type\":\"WITHDRAWAL\"");
        assertThat(historyBody.indexOf("\"type\":\"DEPOSIT\""))
                .isLessThan(historyBody.indexOf("\"type\":\"WITHDRAWAL\""));

        ResponseEntity<BalanceResponse[]> allAccounts =
                restTemplate.getForEntity("/accounts", BalanceResponse[].class);
        assertThat(allAccounts.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void withdrawingMoreThanBalanceReturns400() {
        deposit("carolina", "10.00");

        ResponseEntity<String> response =
                restTemplate.postForEntity("/accounts/carolina/withdrawals", amountRequest("999.00"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Account 'carolina' has insufficient funds for this withdrawal");
    }

    @Test
    void viewingUnknownAccountReturns404() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/accounts/does-not-exist/balance", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).contains("Account 'does-not-exist' not found");
    }

    @Test
    void depositingNegativeAmountReturns400() {
        ResponseEntity<String> response =
                restTemplate.postForEntity("/accounts/david/deposits", amountRequest("-5.00"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Amount must be greater than zero");
    }

    private ResponseEntity<BalanceResponse> deposit(String accountId, String amount) {
        return restTemplate.postForEntity(
                "/accounts/" + accountId + "/deposits", amountRequest(amount), BalanceResponse.class);
    }

    private ResponseEntity<BalanceResponse> withdraw(String accountId, String amount) {
        return restTemplate.postForEntity(
                "/accounts/" + accountId + "/withdrawals", amountRequest(amount), BalanceResponse.class);
    }

    private AmountRequest amountRequest(String amount) {
        AmountRequest request = new AmountRequest();
        request.setAmount(new BigDecimal(amount));
        return request;
    }
}
