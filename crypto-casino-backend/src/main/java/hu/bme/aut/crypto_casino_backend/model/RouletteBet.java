package hu.bme.aut.crypto_casino_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "roulette_bet")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouletteBet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "roulette_result_id", nullable = false)
	private RouletteResult rouletteResult;

	@Column(name = "bet_type", nullable = false)
	@Enumerated(EnumType.STRING)
	private BetType betType;

	@Column(nullable = false)
	private BigInteger amount;

	@Column(nullable = false)
	private Integer number;

	public enum BetType {

		STRAIGHT, RED, BLACK, ODD, EVEN, LOW, HIGH, DOZEN_FIRST, DOZEN_SECOND, DOZEN_THIRD, COLUMN_FIRST, COLUMN_SECOND,
		COLUMN_THIRD

	}

}
