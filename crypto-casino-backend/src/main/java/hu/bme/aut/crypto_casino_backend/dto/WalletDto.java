package hu.bme.aut.crypto_casino_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto {

    private Long id;

    private Long userId;

    private BigDecimal casinoTokenBalance;

    private String walletAddress;

    private boolean blockchainSynced;

    private List<TransactionDto> recentTransactions;
}
