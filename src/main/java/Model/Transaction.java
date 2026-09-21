package Model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Transaction {

    private final UUID id;

    private final TransactionType type;

    private final BigDecimal amount;

    private final Instant timestamp;

    public Transaction(TransactionType type, BigDecimal amount) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.amount = amount;
        this.timestamp = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

}
