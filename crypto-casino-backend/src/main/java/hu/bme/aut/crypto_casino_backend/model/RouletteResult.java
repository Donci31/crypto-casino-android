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

	@Column(nullable = false)
	private BigInteger gameId;

	@OneToMany(mappedBy = "rouletteResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@Builder.Default
	private List<RouletteBet> bets = new ArrayList<>();

	@Column(nullable = false)
	private BigInteger totalBetAmount;

	@Column(nullable = false)
	private String serverSeedHash;

	@Column
	private String serverSeed;

	@Column(nullable = false)
	private String clientSeed;

	@Column
	private Integer winningNumber;

	@Column
	private BigInteger totalPayout;

	@Column(nullable = false)
	private Boolean settled;

}
