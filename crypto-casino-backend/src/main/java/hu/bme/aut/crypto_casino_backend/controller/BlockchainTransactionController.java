package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.transaction.BlockchainTransactionDto;
import hu.bme.aut.crypto_casino_backend.service.BlockchainTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class BlockchainTransactionController {

    private final BlockchainTransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<BlockchainTransactionDto>> getMyTransactions(Principal principal) {
        String username = principal.getName();
        List<BlockchainTransactionDto> transactions = transactionService.getTransactionsForUsername(username);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/{txHash}")
    public ResponseEntity<BlockchainTransactionDto> getTransactionByHash(@PathVariable String txHash, Principal principal) {
        String username = principal.getName();
        return transactionService.getTransactionByHash(txHash, username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
