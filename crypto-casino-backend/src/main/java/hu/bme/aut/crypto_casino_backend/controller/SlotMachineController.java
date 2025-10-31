package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.game.GameHistoryResponse;
import hu.bme.aut.crypto_casino_backend.dto.game.SlotConfigResponse;
import hu.bme.aut.crypto_casino_backend.dto.game.SpinRequest;
import hu.bme.aut.crypto_casino_backend.dto.game.SpinResponse;
import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import hu.bme.aut.crypto_casino_backend.service.SlotMachineService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/games/slots")
@RequiredArgsConstructor
public class SlotMachineController {

	private final SlotMachineService slotMachineService;

	@PostMapping("/spin")
	public ResponseEntity<SpinResponse> spin(@AuthenticationPrincipal UserPrincipal currentUser,
			@Valid @RequestBody SpinRequest request) {
		log.info("Spin request from user: {}, bet: {}", currentUser.getUsername(), request.getBetAmount());

		SpinResponse response = slotMachineService.executeSpin(currentUser.getId(), request.getBetAmount());

		log.info("Spin result for user {}: reels={}, win={}", currentUser.getUsername(), response.getReels(),
				response.getWinAmount());

		return ResponseEntity.ok(response);
	}

	@GetMapping("/config")
	public ResponseEntity<SlotConfigResponse> getConfig() {
		log.info("Slot config request");
		return ResponseEntity.ok(slotMachineService.getSlotMachineConfig());
	}

	@GetMapping("/history")
	public ResponseEntity<List<GameHistoryResponse>> getHistory(@AuthenticationPrincipal UserPrincipal currentUser) {
		log.info("Game history request for user: {}", currentUser.getUsername());
		List<GameHistoryResponse> history = slotMachineService.getGameHistory(currentUser.getId());
		log.info("Returning {} game history entries", history.size());
		return ResponseEntity.ok(history);
	}

	@GetMapping("/balance")
	public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal UserPrincipal currentUser) {
		log.info("Vault balance request for user: {}", currentUser.getUsername());
		if (currentUser.getPrimaryWalletAddress() == null) {
			log.warn("User {} has no primary wallet address", currentUser.getUsername());
			return ResponseEntity.badRequest().build();
		}

		BigDecimal balance = slotMachineService.getVaultBalance(currentUser.getPrimaryWalletAddress());
		log.info("Vault balance for user {}: {}", currentUser.getUsername(), balance);
		return ResponseEntity.ok(new BalanceResponse(balance));
	}

	public record BalanceResponse(BigDecimal balance) {
	}

}
