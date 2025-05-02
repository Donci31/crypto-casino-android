package hu.bme.aut.crypto_casino_backend.dto.game;

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
public class SpinResponse {
    private Long spinId;
    private int[] reels;
    private BigDecimal betAmount;
    private BigDecimal winAmount;
    private boolean isWin;
    private LocalDateTime timestamp;
    private BigDecimal newBalance;
}
