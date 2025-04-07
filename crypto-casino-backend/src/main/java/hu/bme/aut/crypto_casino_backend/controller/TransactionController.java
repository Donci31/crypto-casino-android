package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.TransactionDto;
import hu.bme.aut.crypto_casino_backend.mapper.TransactionMapper;
import hu.bme.aut.crypto_casino_backend.model.Transaction;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.repository.TransactionRepository;
import hu.bme.aut.crypto_casino_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final TransactionMapper transactionMapper;

    
    @GetMapping
    public ResponseEntity<List<TransactionDto>> getUserTransactions(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        User user = userService.getUserByUsername(principal.getName());

        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionTime").descending());

        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionTimeDesc(user);

        List<TransactionDto> responseList = transactionMapper.toDtoList(transactions);

        return ResponseEntity.ok(responseList);
    }

    
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(
            Principal principal,
            @PathVariable Long id
    ) {
        User user = userService.getUserByUsername(principal.getName());

        Transaction transaction = transactionRepository.findById(id)
                .orElse(null);

        if (transaction == null || !transaction.getUser().getId().equals(user.getId())) {
            return ResponseEntity.notFound().build();
        }

        TransactionDto transactionDto = transactionMapper.toDto(transaction);

        return ResponseEntity.ok(transactionDto);
    }

    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getTransactionStats(Principal principal) {
        User user = userService.getUserByUsername(principal.getName());

        List<Transaction> userTransactions = transactionRepository.findByUserOrderByTransactionTimeDesc(user);


        Map<String, Long> statsByType = userTransactions.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        transaction -> transaction.getType().name(),
                        java.util.stream.Collectors.counting()
                ));

        return ResponseEntity.ok(statsByType);
    }
}
