package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.model.DiceResult;
import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import hu.bme.aut.crypto_casino_backend.service.DiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/games/dice")
@RequiredArgsConstructor
public class DiceController {

	private final DiceService diceService;

	@PostMapping("/create")
	public CompletableFuture<ResponseEntity<DiceService.DiceGameCreatedResponse>> createGame(
			@AuthenticationPrincipal UserPrincipal currentUser, @Valid @RequestBody DiceGameRequest request) {
		log.info("Dice game creation request from user: {}, bet: {}, prediction: {}, betType: {}",
				currentUser.getUsername(), request.betAmount(), request.prediction(), request.betType());

		return diceService
			.createGame(currentUser.getId(), request.betAmount(), request.prediction(), request.betType(),
					request.clientSeed())
			.thenApply(response -> {
				log.info("Dice game created for user {}: gameId={}, serverSeedHash={}", currentUser.getUsername(),
						response.getGameId(), response.getServerSeedHash());
				return ResponseEntity.ok(response);
			});
	}

	@PostMapping("/settle/{gameId}")
	public CompletableFuture<ResponseEntity<DiceService.DiceGameSettledResponse>> settleGame(
			@AuthenticationPrincipal UserPrincipal currentUser, @PathVariable Long gameId) {
		log.info("Dice game settlement request from user: {}, gameId: {}", currentUser.getUsername(), gameId);

		return diceService.settleGame(currentUser.getId(), gameId).thenApply(response -> {
			log.info("Dice game settled for user {}: gameId={}, result={}, won={}", currentUser.getUsername(),
					response.getGameId(), response.getResult(), response.isWon());
			return ResponseEntity.ok(response);
		});
	}

	@GetMapping("/status/{gameId}")
	public ResponseEntity<DiceService.DiceGameStatusResponse> getGameStatus(
			@AuthenticationPrincipal UserPrincipal currentUser, @PathVariable Long gameId) {
		log.info("Dice game status request from user: {}, gameId: {}", currentUser.getUsername(), gameId);
		DiceService.DiceGameStatusResponse status = diceService.getGameStatus(currentUser.getId(), gameId);
		return ResponseEntity.ok(status);
	}

	@GetMapping("/config")
	public ResponseEntity<DiceService.DiceConfigResponse> getConfig() {
		log.info("Dice config request");
		return ResponseEntity.ok(diceService.getDiceConfig());
	}

	public record DiceGameRequest(BigDecimal betAmount, Integer prediction, DiceResult.BetType betType,
			String clientSeed) {
	}

}
