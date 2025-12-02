package hu.bme.aut.crypto_casino_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "roulette_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouletteResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "game_session_id", nullable = false, unique = true)
  private GameSession gameSession;

  @Column(name = "game_id", nullable = false)
  private BigInteger gameId;

  @OneToMany(mappedBy = "rouletteResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  @Builder.Default
  private List<RouletteBet> bets = new ArrayList<>();

  @Column(name = "total_bet_amount", nullable = false)
  private BigInteger totalBetAmount;

  @Column(name = "server_seed_hash", nullable = false)
  private String serverSeedHash;

  @Column(name = "server_seed")
  private String serverSeed;

  @Column(name = "client_seed", nullable = false)
  private String clientSeed;

  @Column(name = "winning_number")
  private Integer winningNumber;

  @Column(name = "total_payout")
  private BigInteger totalPayout;

  @Column(nullable = false)
  private Boolean settled;

}
