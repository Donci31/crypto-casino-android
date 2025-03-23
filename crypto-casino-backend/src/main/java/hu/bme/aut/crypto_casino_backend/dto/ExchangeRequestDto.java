package hu.bme.aut.crypto_casino_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequestDto {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0001", message = "Minimum exchange amount is 0.0001 ETH")
    private BigDecimal ethAmount;

    private String destinationAddress;
}
