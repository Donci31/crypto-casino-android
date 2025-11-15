package hu.bme.aut.crypto_casino_backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuickStatsResponse {

	private BigDecimal vaultBalance;

	private BigDecimal walletBalance;

	private Integer totalGamesPlayed;

	private BigDecimal totalWinnings;

	private Double winRate;

	private BigDecimal biggestWin;

}
