package hu.bme.aut.crypto_casino_backend.dto.game;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameHistoryResponse {

  private Long spinId;

  private int[] reels;

  private BigDecimal betAmount;

  private BigDecimal winAmount;

  private boolean isWin;

  private LocalDateTime timestamp;

}
