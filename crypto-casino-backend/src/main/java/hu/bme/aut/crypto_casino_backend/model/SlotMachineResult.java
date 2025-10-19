package hu.bme.aut.crypto_casino_backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "slot_machine_result")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotMachineResult {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "game_session_id", nullable = false, unique = true)
	private GameSession gameSession;

	@Column(nullable = false)
	private Integer reel1;

	@Column(nullable = false)
	private Integer reel2;

	@Column(nullable = false)
	private Integer reel3;

	@Column(nullable = false)
	private Long spinId;

}
