# Tiny Ledger App

A small in-memory ledger service, exposed as a REST API. Supports recording deposits and withdrawals, viewing an account's current balance, and viewing its transaction history.

## Running it

Requires only Java 21+ and Maven.

```bash
mvn spring-boot:run
```

The server starts on `http://localhost:8080`. Data is stored **in memory** and is lost when the process stops.

An interactive Swagger UI is available once the app is running, at `http://localhost:8080/swagger-ui/index.html` (raw OpenAPI spec at `/v3/api-docs`).

## Running the tests

```bash
mvn test
```

Includes unit tests for the core ledger logic (`Service.LedgerServiceTest`) and end-to-end HTTP tests against a real running instance (`Controller.LedgerApiTest`).

## API

Every endpoint operates on an `accountId` you choose (any string). There's no account-creation step and depositing into a new `accountId` opens it implicitly.

### Deposit

```bash
curl -X POST http://localhost:8080/accounts/alice/deposits \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00}'
```
```json
{"accountId":"alice","balance":100.00}
```

### Withdraw

```bash
curl -X POST http://localhost:8080/accounts/alice/withdrawals \
  -H "Content-Type: application/json" \
  -d '{"amount": 30.00}'
```
```json
{"accountId":"alice","balance":70.00}
```

### View balance

```bash
curl http://localhost:8080/accounts/alice/balance
```
```json
{"accountId":"alice","balance":70.00}
```

### View transaction history

```bash
curl http://localhost:8080/accounts/alice/transactions
```
```json
[
  {"type":"DEPOSIT","amount":100.00,"timestamp":"2026-09-18T11:13:15.429418Z"},
  {"type":"WITHDRAWAL","amount":30.00,"timestamp":"2026-09-18T11:13:15.488778Z"}
]
```

### List all accounts

```bash
curl http://localhost:8080/accounts
```
```json
[{"accountId":"alice","balance":70.00}]
```

### Error responses

Errors use Spring Boot's default error body shape, with the failure reason in `message`:

- Withdrawing more than the balance → `400`, `{"message":"Account 'alice' has insufficient funds for this withdrawal", ...}`
- Depositing/withdrawing a non-positive amount, or one with more than 2 decimal places → `400`, `{"message":"...", ...}`
- Viewing the balance/transactions of, or withdrawing from, an account that was never deposited into → `404`, `{"message":"Account 'bob' not found", ...}`

## Assumptions

- No concept of currency exists every amount is an implicit single currency; nothing in the API distinguishes between them.
- An account is identified by any caller-supplied string id, and is created implicitly on its first deposit. Withdrawing from, or viewing the balance or history of, an id that has never been deposited into returns `404` rather than a zero balance.
- Amounts are limited to at most 2 decimal places and must be strictly greater than zero.
- Storage is a single in-memory map (`ConcurrentHashMap`).