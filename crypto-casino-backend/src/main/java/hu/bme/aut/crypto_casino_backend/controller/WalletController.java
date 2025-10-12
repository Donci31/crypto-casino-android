package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.wallet.BalanceResponse;
import hu.bme.aut.crypto_casino_backend.dto.wallet.SetPrimaryRequest;
import hu.bme.aut.crypto_casino_backend.dto.wallet.WalletRequest;
import hu.bme.aut.crypto_casino_backend.dto.wallet.WalletResponse;
import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import hu.bme.aut.crypto_casino_backend.service.WalletService;
import jakarta.validation.Valid;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletResponse> addWallet(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody WalletRequest request
    ) {
        log.info("Add wallet request for user: {}, address: {}", currentUser.getUsername(), request.getAddress());
        WalletResponse response = walletService.addWallet(currentUser.getId(), request);
        log.info("Wallet added successfully: {}", response.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WalletResponse>> getUserWallets(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        log.info("Get wallets request for user: {}", currentUser.getUsername());
        List<WalletResponse> wallets = walletService.getUserWallets(currentUser.getId());
        log.info("Returning {} wallets", wallets.size());
        return ResponseEntity.ok(wallets);
    }

    @PutMapping("/primary")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletResponse> setPrimaryWallet(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SetPrimaryRequest request
    ) {
        log.info("Set primary wallet request for user: {}, walletId: {}", currentUser.getUsername(),
                request.getWalletId());
        WalletResponse response = walletService.setPrimaryWallet(currentUser.getId(), request);
        log.info("Primary wallet set successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BalanceResponse> getWalletBalance(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long walletId
    ) {
        log.info("Get balance request for user: {}, walletId: {}", currentUser.getUsername(), walletId);
        BalanceResponse balance = walletService.getWalletBalance(currentUser.getId(), walletId);
        log.info("Balance retrieved for address: {}, balance: {}", balance.getAddress(), balance.getBalance());
        return ResponseEntity.ok(balance);
    }

}
