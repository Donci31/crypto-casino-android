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
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/slots")
@RequiredArgsConstructor
public class SlotMachineController {

	private final SlotMachineService slotMachineService;

	@PostMapping("/spin")
	@PreAuthorize("hasRole('USER')")
	public CompletableFuture<ResponseEntity<SpinResponse>> spin(@AuthenticationPrincipal UserPrincipal currentUser,
			@Valid @RequestBody SpinRequest request) {

		return slotMachineService.executeSpin(currentUser.getId(), request.getBetAmount())
			.thenApply(ResponseEntity::ok);
	}

	@GetMapping("/config")
	public ResponseEntity<SlotConfigResponse> getConfig() {
		return ResponseEntity.ok(slotMachineService.getSlotMachineConfig());
	}

	@GetMapping("/history")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<List<GameHistoryResponse>> getHistory(@AuthenticationPrincipal UserPrincipal currentUser) {

		return ResponseEntity.ok(slotMachineService.getGameHistory(currentUser.getId()));
	}

	@GetMapping("/balance")
	@PreAuthorize("hasRole('USER')")
	public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal UserPrincipal currentUser) {
		if (currentUser.getPrimaryWalletAddress() == null) {
			return ResponseEntity.badRequest().build();
		}

		BigDecimal balance = slotMachineService.getVaultBalance(currentUser.getPrimaryWalletAddress());
		return ResponseEntity.ok(new BalanceResponse(balance));
	}

	public record BalanceResponse(BigDecimal balance) {
	}

}
