package hu.bme.aut.crypto_casino_backend.dto.transaction;

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
public class TransactionDto {

    private Long id;

    private Long userId;

    private Long walletId;

    private BigDecimal amount;

    private String type;

    private String transactionHash;

    private Long blockNumber;

    private BigDecimal ethereumAmount;

    private BigDecimal casinoTokenAmount;

    private LocalDateTime transactionTime;

    private String status;
}
