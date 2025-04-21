package hu.bme.aut.crypto_casino_backend.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String transactionHash;
    private String status;
    private LocalDateTime timestamp;
    private BigDecimal ethereumAmount;
    private BigDecimal tokenAmount;
}
