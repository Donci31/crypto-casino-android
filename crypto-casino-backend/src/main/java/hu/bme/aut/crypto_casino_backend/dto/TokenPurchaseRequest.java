package hu.bme.aut.crypto_casino_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenPurchaseRequest {
    private BigDecimal ethAmount;
}
