package hu.bme.aut.crypto_casino_backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

  private Integer totalGamesPlayed;

  private Double winRate;

  private BigDecimal totalWinnings;

  private BigDecimal totalLosses;

  private BigDecimal netProfitLoss;

  private BigDecimal biggestWin;

  private String mostPlayedGame;

  private BigDecimal totalDeposited;

  private BigDecimal totalWithdrawn;

  private BigDecimal totalWagered;

  private BigDecimal houseEdgePaid;

  private Map<String, GameStatsDto> gameStats;

}
