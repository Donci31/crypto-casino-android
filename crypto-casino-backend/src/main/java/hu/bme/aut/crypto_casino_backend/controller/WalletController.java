package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.wallet.BalanceResponse;
import hu.bme.aut.crypto_casino_backend.dto.wallet.SetPrimaryRequest;
import hu.bme.aut.crypto_casino_backend.dto.wallet.WalletRequest;
import hu.bme.aut.crypto_casino_backend.dto.wallet.WalletResponse;
import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import hu.bme.aut.crypto_casino_backend.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;

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

        WalletResponse response = walletService.addWallet(currentUser.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<WalletResponse>> getUserWallets(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {

        List<WalletResponse> wallets = walletService.getUserWallets(currentUser.getId());
        return ResponseEntity.ok(wallets);
    }

    @PutMapping("/primary")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<WalletResponse> setPrimaryWallet(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SetPrimaryRequest request
    ) {

        WalletResponse response = walletService.setPrimaryWallet(currentUser.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{walletId}/balance")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BalanceResponse> getWalletBalance(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long walletId
    ) {

        BalanceResponse balance = walletService.getWalletBalance(currentUser.getId(), walletId);
        return ResponseEntity.ok(balance);
    }
}
