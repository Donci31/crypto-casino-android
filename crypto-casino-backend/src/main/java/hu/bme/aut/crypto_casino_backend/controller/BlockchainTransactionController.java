package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.transaction.BlockchainTransactionDto;
import hu.bme.aut.crypto_casino_backend.security.UserPrincipal;
import hu.bme.aut.crypto_casino_backend.service.BlockchainTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class BlockchainTransactionController {

    private final BlockchainTransactionService transactionService;

    @GetMapping
    public ResponseEntity<Page<BlockchainTransactionDto>> getMyTransactions(
            @AuthenticationPrincipal UserPrincipal principal,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        String username = principal.getUsername();
        Page<BlockchainTransactionDto> transactions = transactionService.getTransactionsForUsername(username, pageable);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{txHash}")
    public ResponseEntity<BlockchainTransactionDto> getTransactionByHash(
            @PathVariable String txHash,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String username = principal.getUsername();
        return transactionService.getTransactionByHash(txHash, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
