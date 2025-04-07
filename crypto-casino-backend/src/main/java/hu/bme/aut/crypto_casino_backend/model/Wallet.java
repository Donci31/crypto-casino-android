package hu.bme.aut.crypto_casino_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "wallets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ethereum_address", unique = true)
    private String ethereumAddress;

    @Column(name = "encrypted_key", length = 1000)
    private String encryptedKey;

    @Column(name = "casino_token_balance", nullable = false)
    private BigDecimal casinoTokenBalance;

    @Column(name = "ethereum_balance")
    private BigDecimal ethereumBalance;

    @Column(name = "blockchain_registered")
    private boolean blockchainRegistered;

    @Column(name = "last_synced")
    private Long lastSynced;

    @OneToMany(mappedBy = "wallet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        if (casinoTokenBalance == null) {
            casinoTokenBalance = BigDecimal.ZERO;
        }
        if (ethereumBalance == null) {
            ethereumBalance = BigDecimal.ZERO;
        }
    }
}
