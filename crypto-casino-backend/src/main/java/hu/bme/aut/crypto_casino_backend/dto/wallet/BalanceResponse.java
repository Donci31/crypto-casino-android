package hu.bme.aut.crypto_casino_backend.dto.wallet;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceResponse {

	private String address;

	private BigDecimal balance;

}
