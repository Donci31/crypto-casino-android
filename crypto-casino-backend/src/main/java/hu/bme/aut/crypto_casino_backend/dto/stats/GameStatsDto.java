package hu.bme.aut.crypto_casino_backend.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameStatsDto {

  private Integer played;

  private Integer wins;

  private Integer losses;

  private Double winRate;

}
