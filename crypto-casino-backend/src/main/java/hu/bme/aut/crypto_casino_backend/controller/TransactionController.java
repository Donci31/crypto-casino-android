package hu.bme.aut.crypto_casino_backend.controller;

import hu.bme.aut.crypto_casino_backend.dto.transaction.TransactionDto;
import hu.bme.aut.crypto_casino_backend.mapper.TransactionMapper;
import hu.bme.aut.crypto_casino_backend.model.Transaction;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.repository.TransactionRepository;
import hu.bme.aut.crypto_casino_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    private final TransactionMapper transactionMapper;

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getUserTransactions(
            Principal principal
    ) {
        User user = userService.getUserByUsername(principal.getName());

        List<Transaction> transactions = transactionRepository.findByUser(user);

        List<TransactionDto> transactionDtoList = transactionMapper.toDtoList(transactions);

        return ResponseEntity.ok(transactionDtoList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransactionById(
            Principal principal,
            @PathVariable Long id
    ) {
        User user = userService.getUserByUsername(principal.getName());

        return transactionRepository.findById(id)
                .filter(transaction -> transaction.getUser().getId().equals(user.getId()))
                .map(transactionMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

