package hu.bme.aut.crypto_casino_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "game_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "game_type", nullable = false, length = 50)
	private String gameType;

	@Column(name = "bet_amount", nullable = false, precision = 38, scale = 18)
	private BigDecimal betAmount;

	@Column(name = "win_amount", nullable = false, precision = 38, scale = 18)
	private BigDecimal winAmount;

	@Column(name = "is_resolved", nullable = false)
	private Boolean isResolved;

	@Column(name = "blockchain_tx_hash", length = 66)
	private String blockchainTxHash;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "resolved_at")
	private LocalDateTime resolvedAt;

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		isResolved = false;
	}

}
