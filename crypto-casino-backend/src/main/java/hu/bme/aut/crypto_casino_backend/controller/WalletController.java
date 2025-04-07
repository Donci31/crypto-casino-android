package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.ExchangeRequestDto;
import hu.bme.aut.crypto_casino_backend.dto.TransactionDto;
import hu.bme.aut.crypto_casino_backend.dto.WalletDto;
import hu.bme.aut.crypto_casino_backend.mapper.TransactionMapper;
import hu.bme.aut.crypto_casino_backend.mapper.WalletMapper;
import hu.bme.aut.crypto_casino_backend.model.Transaction;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.model.Wallet;
import hu.bme.aut.crypto_casino_backend.repository.TransactionRepository;
import hu.bme.aut.crypto_casino_backend.service.UserService;
import hu.bme.aut.crypto_casino_backend.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.tuples.generated.Tuple2;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private static final Logger log = LoggerFactory.getLogger(WalletController.class);
    private final WalletService walletService;
    private final UserService userService;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    
    @PostMapping("/initialize")
    public ResponseEntity<WalletDto> initializeWallet(Principal principal) throws Exception {
        User user = userService.getUserByUsername(principal.getName());

        if (user.isWalletInitialized() && user.getWallet() != null) {
            
            walletService.syncWalletWithBlockchain(user.getWallet());

            
            List<Transaction> recentTransactions = transactionRepository
                    .findByWalletOrderByTransactionTimeDesc(user.getWallet());

            
            WalletDto walletDto = walletMapper.toDto(user.getWallet());

            return ResponseEntity.ok(walletDto);
        }

        
        Wallet wallet = walletService.createWalletForUser(user);

        
        walletService.registerWalletOnBlockchain(wallet);

        
        WalletDto walletDto = walletMapper.toDto(wallet);

        return ResponseEntity.ok(walletDto);
    }

    
    @GetMapping
    public ResponseEntity<WalletDto> getWallet(Principal principal) throws Exception {
        User user = userService.getUserByUsername(principal.getName());

        if (!user.isWalletInitialized() || user.getWallet() == null) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Getting wallet for user {}", user.getWallet().getEthereumAddress());

        
        walletService.syncWalletWithBlockchain(user.getWallet());

        
        List<Transaction> recentTransactions = transactionRepository
                .findByWalletOrderByTransactionTimeDesc(user.getWallet());

        
        WalletDto walletDto = walletMapper.toDto(user.getWallet());

        return ResponseEntity.ok(walletDto);
    }

    
    @PostMapping("/purchase-tokens")
    public ResponseEntity<TransactionDto> purchaseTokens(
            Principal principal,
            @Valid @RequestBody ExchangeRequestDto request
    ) throws Exception {
        User user = userService.getUserByUsername(principal.getName());

        if (!user.isWalletInitialized() || user.getWallet() == null) {
            return ResponseEntity.badRequest().build();
        }

        
        Transaction transaction = walletService.purchaseTokens(user.getWallet(), request.getEthAmount());

        
        TransactionDto transactionDto = transactionMapper.toDto(transaction);

        return ResponseEntity.ok(transactionDto);
    }

    
    @PostMapping("/withdraw-tokens")
    public ResponseEntity<TransactionDto> withdrawTokens(
            Principal principal,
            @Valid @RequestBody ExchangeRequestDto request
    ) throws Exception {
        User user = userService.getUserByUsername(principal.getName());

        if (!user.isWalletInitialized() || user.getWallet() == null) {
            return ResponseEntity.badRequest().build();
        }

        
        BigDecimal tokenAmount = request.getEthAmount().multiply(BigDecimal.valueOf(100));

        
        Transaction transaction = walletService.withdrawTokens(user.getWallet(), tokenAmount);

        
        TransactionDto transactionDto = transactionMapper.toDto(transaction);

        return ResponseEntity.ok(transactionDto);
    }

    
    @PostMapping("/deposit")
    public ResponseEntity<TransactionDto> depositToCasino(
            Principal principal,
            @Valid @RequestBody Map<String, BigDecimal> request
    ) throws Exception {
        User user = userService.getUserByUsername(principal.getName());

        if (!user.isWalletInitialized() || user.getWallet() == null) {
            return ResponseEntity.badRequest().build();
        }

        
        BigDecimal tokenAmount = request.get("tokenAmount");
        if (tokenAmount == null || tokenAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        
        Transaction transaction = walletService.depositTokens(user.getWallet(), tokenAmount);

        
        TransactionDto transactionDto = transactionMapper.toDto(transaction);

        return ResponseEntity.ok(transactionDto);
    }

    
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionDto> withdrawFromCasino(
            Principal principal,
            @Valid @RequestBody Map<String, BigDecimal> request
    ) throws Exception {
        User user = userService.getUserByUsername(principal.getName());

        if (!user.isWalletInitialized() || user.getWallet() == null) {
            return ResponseEntity.badRequest().build();
        }

        
        BigDecimal tokenAmount = request.get("tokenAmount");
        if (tokenAmount == null || tokenAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseEntity.badRequest().body(null);
        }

        
        Transaction transaction = walletService.withdrawFromCasino(user.getWallet(), tokenAmount);

        
        TransactionDto transactionDto = transactionMapper.toDto(transaction);

        return ResponseEntity.ok(transactionDto);
    }

    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, String>> getUserStats(Principal principal) throws Exception {
        User user = userService.getUserByUsername(principal.getName());

        if (!user.isWalletInitialized() || user.getWallet() == null) {
            return ResponseEntity.badRequest().build();
        }

        
        CompletableFuture<Tuple2<BigInteger, BigInteger>> statsFuture =
                walletService.getUserStats(user.getWallet());

        Tuple2<BigInteger, BigInteger> stats = statsFuture.join();

        Map<String, String> response = new HashMap<>();
        response.put("totalBets", stats.component1().toString());
        response.put("totalWins", stats.component2().toString());

        return ResponseEntity.ok(response);
    }
}
