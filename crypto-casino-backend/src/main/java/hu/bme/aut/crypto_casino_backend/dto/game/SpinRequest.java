package hu.bme.aut.crypto_casino_backend.dto.game;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinRequest {

	@NotNull(message = "Bet amount is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Bet amount must be greater than 0")
	private BigDecimal betAmount;

}
