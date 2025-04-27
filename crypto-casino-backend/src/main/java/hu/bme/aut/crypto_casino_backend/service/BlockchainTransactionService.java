package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.dto.transaction.BlockchainTransactionDto;
import hu.bme.aut.crypto_casino_backend.mapper.BlockchainTransactionMapper;
import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import hu.bme.aut.crypto_casino_backend.repository.BlockchainTransactionRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlockchainTransactionService {

    private final BlockchainTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BlockchainTransactionMapper transactionMapper;

    public List<BlockchainTransactionDto> getTransactionsForUsername(String username) {
        String userAddress = getUserAddressByUsername(username);
        List<BlockchainTransaction> transactions = transactionRepository.findByUserAddressOrderByTimestampDesc(userAddress);
        return transactionMapper.toDtoList(transactions);
    }

    public Optional<BlockchainTransactionDto> getTransactionByHash(String txHash, String username) {
        String userAddress = getUserAddressByUsername(username);

        return transactionRepository.findByTxHashAndUserAddress(txHash, userAddress)
                .map(transactionMapper::toDto);
    }

    private String getUserAddressByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getWalletAddress();
    }
}
