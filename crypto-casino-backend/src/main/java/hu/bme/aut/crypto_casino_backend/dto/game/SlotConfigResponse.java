package hu.bme.aut.crypto_casino_backend.dto.game;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotConfigResponse {

	private BigDecimal minBet;

	private BigDecimal maxBet;

	private int houseEdge;

	private boolean isActive;

}
