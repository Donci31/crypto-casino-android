package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import hu.bme.aut.crypto_casino_backend.service.RouletteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/games/roulette")
@RequiredArgsConstructor
public class RouletteController {

  private final RouletteService rouletteService;

  @PostMapping("/create")
  public ResponseEntity<RouletteService.RouletteGameCreatedResponse> createGame(
      @AuthenticationPrincipal UserPrincipal currentUser, @Valid @RequestBody RouletteGameRequest request) {
    log.info("Roulette game creation request from user: {}, bets count: {}", currentUser.getUsername(),
        request.bets().size());

    RouletteService.RouletteGameCreatedResponse response = rouletteService.createGame(currentUser.getId(),
        request.bets(), request.clientSeed());

    log.info("Roulette game created for user {}: gameId={}, serverSeedHash={}", currentUser.getUsername(),
        response.getGameId(), response.getServerSeedHash());

    return ResponseEntity.ok(response);
  }

  @PostMapping("/settle/{gameId}")
  public ResponseEntity<RouletteService.RouletteGameSettledResponse> settleGame(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable Long gameId) {
    log.info("Roulette game settlement request from user: {}, gameId: {}", currentUser.getUsername(), gameId);

    RouletteService.RouletteGameSettledResponse response = rouletteService.settleGame(currentUser.getId(), gameId);

    log.info("Roulette game settled for user {}: gameId={}, winningNumber={}, totalPayout={}",
        currentUser.getUsername(), response.getGameId(), response.getWinningNumber(),
        response.getTotalPayout());

    return ResponseEntity.ok(response);
  }

  @GetMapping("/status/{gameId}")
  public ResponseEntity<RouletteService.RouletteGameStatusResponse> getGameStatus(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable Long gameId) {
    log.info("Roulette game status request from user: {}, gameId: {}", currentUser.getUsername(), gameId);
    RouletteService.RouletteGameStatusResponse status = rouletteService.getGameStatus(currentUser.getId(), gameId);
    return ResponseEntity.ok(status);
  }

  @GetMapping("/config")
  public ResponseEntity<RouletteService.RouletteConfigResponse> getConfig() {
    log.info("Roulette config request");
    return ResponseEntity.ok(rouletteService.getRouletteConfig());
  }

  @GetMapping("/balance")
  public ResponseEntity<BalanceResponse> getBalance(@AuthenticationPrincipal UserPrincipal currentUser) {
    log.info("Vault balance request for user: {}", currentUser.getUsername());
    if (currentUser.getPrimaryWalletAddress() == null) {
      log.warn("User {} has no primary wallet address", currentUser.getUsername());
      return ResponseEntity.badRequest().build();
    }

    BigDecimal balance = rouletteService.getVaultBalance(currentUser.getPrimaryWalletAddress());
    log.info("Vault balance for user {}: {}", currentUser.getUsername(), balance);
    return ResponseEntity.ok(new BalanceResponse(balance));
  }

  public record RouletteGameRequest(List<RouletteService.BetRequest> bets, String clientSeed) {
  }

  public record BalanceResponse(BigDecimal balance) {
  }

}
