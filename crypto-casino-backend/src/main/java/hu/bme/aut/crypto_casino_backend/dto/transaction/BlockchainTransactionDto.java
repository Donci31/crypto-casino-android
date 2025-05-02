package hu.bme.aut.crypto_casino_backend.dto.transaction;

import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockchainTransactionDto {
    private String txHash;
    private Long blockNumber;
    private Integer logIndex;
    private String userAddress;
    private BlockchainTransaction.TransactionType eventType;
    private BigDecimal amount;
    private BigDecimal newBalance;
    private String gameAddress;
    private LocalDateTime timestamp;
}
