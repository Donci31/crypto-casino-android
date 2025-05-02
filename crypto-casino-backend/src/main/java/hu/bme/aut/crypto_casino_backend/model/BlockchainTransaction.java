package hu.bme.aut.crypto_casino_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "blockchain_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 66)
    private String txHash;

    private Long blockNumber;

    private Integer logIndex;

    @Column(nullable = false, length = 42)
    private String userAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType eventType;

    @Column(nullable = false, precision = 38, scale = 18)
    private BigDecimal amount;

    @Column(precision = 38, scale = 18)
    private BigDecimal newBalance;

    @Column(length = 42)
    private String gameAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        BET,
        WIN,
        TOKEN_PURCHASED,
        TOKEN_EXCHANGED
    }
}
