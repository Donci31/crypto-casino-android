package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.dto.transaction.BlockchainTransactionDto;
import hu.bme.aut.crypto_casino_backend.exception.ResourceNotFoundException;
import hu.bme.aut.crypto_casino_backend.mapper.BlockchainTransactionMapper;
import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.repository.BlockchainTransactionRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlockchainTransactionService {

    private final UserRepository userRepository;
    private final BlockchainTransactionRepository transactionRepository;
    private final BlockchainTransactionMapper transactionMapper;

    public List<BlockchainTransactionDto> getTransactionsForUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        List<BlockchainTransaction> txs = transactionRepository.findByUserAddressOrderByTimestampDesc(user.getWalletAddress());
        return transactionMapper.toDtoList(txs);
    }
}
