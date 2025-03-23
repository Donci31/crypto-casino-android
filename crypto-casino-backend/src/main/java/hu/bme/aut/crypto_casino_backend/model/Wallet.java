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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "casino_token_balance", nullable = false)
    private BigDecimal casinoTokenBalance;

    @Column(name = "wallet_address", unique = true)
    private String walletAddress;

    @Column(name = "blockchain_synced")
    private boolean blockchainSynced;

    @OneToMany(mappedBy = "wallet")
    private Set<Transaction> transactions = new HashSet<>();
}
