package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.stats.QuickStatsResponse;
import hu.bme.aut.crypto_casino_backend.dto.stats.UserStatsResponse;
import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import hu.bme.aut.crypto_casino_backend.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class UserStatsController {

  private final UserStatsService userStatsService;

  @GetMapping
  public ResponseEntity<UserStatsResponse> getUserStats(@AuthenticationPrincipal UserPrincipal currentUser) {
    UserStatsResponse stats = userStatsService.getUserStats(currentUser.getId());
    return ResponseEntity.ok(stats);
  }

  @GetMapping("/quick")
  public ResponseEntity<QuickStatsResponse> getQuickStats(@AuthenticationPrincipal UserPrincipal currentUser) {
    QuickStatsResponse quickStats = userStatsService.getQuickStats(currentUser.getId());
    return ResponseEntity.ok(quickStats);
  }

}
