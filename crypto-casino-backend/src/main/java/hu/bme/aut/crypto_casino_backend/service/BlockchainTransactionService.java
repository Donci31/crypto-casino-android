package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.dto.transaction.BlockchainTransactionDto;
import hu.bme.aut.crypto_casino_backend.mapper.BlockchainTransactionMapper;
import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import hu.bme.aut.crypto_casino_backend.repository.BlockchainTransactionRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlockchainTransactionService {

	private final BlockchainTransactionRepository transactionRepository;

	private final UserRepository userRepository;

	private final BlockchainTransactionMapper transactionMapper;

	public Page<BlockchainTransactionDto> getTransactionsForUsername(String username, Pageable pageable) {
		String userAddress = getUserAddressByUsername(username);
		Page<BlockchainTransaction> transactionsPage = transactionRepository
			.findByUserAddressOrderByTimestampDesc(userAddress, pageable);
		return transactionsPage.map(transactionMapper::toDto);
	}

	public Optional<BlockchainTransactionDto> getTransactionByHash(String txHash, String username) {
		String userAddress = getUserAddressByUsername(username);
		return transactionRepository.findByTxHash(txHash)
			.stream()
			.filter(tx -> tx.getUserAddress().equals(userAddress))
			.map(transactionMapper::toDto)
			.findFirst();
	}

	private String getUserAddressByUsername(String username) {
		return userRepository.findByUsername(username)
			.orElseThrow(() -> new IllegalArgumentException("User not found"))
			.getPrimaryWallet()
			.getAddress();
	}

}
