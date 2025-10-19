package hu.bme.aut.crypto_casino_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "dice_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiceResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "game_session_id", nullable = false, unique = true)
	private GameSession gameSession;

	@Column(nullable = false)
	private BigInteger gameId;

	@Column(nullable = false)
	private Integer prediction;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private BetType betType;

	@Column(nullable = false)
	private String serverSeedHash;

	@Column
	private String serverSeed;

	@Column(nullable = false)
	private String clientSeed;

	@Column
	private Integer result;

	@Column
	private BigInteger payout;

	@Column(nullable = false)
	private Boolean settled;

	public enum BetType {

		ROLL_UNDER, ROLL_OVER, EXACT

	}

}
