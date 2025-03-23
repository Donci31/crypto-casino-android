package hu.bme.aut.crypto_casino_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletSummaryDto {

    private Long id;

    private BigDecimal casinoTokenBalance;

    private String walletAddress;

    private int transactionCount;
}
