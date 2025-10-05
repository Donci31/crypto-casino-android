package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.dto.wallet.BalanceResponse;
import hu.bme.aut.crypto_casino_backend.dto.wallet.SetPrimaryRequest;
import hu.bme.aut.crypto_casino_backend.dto.wallet.WalletRequest;
import hu.bme.aut.crypto_casino_backend.dto.wallet.WalletResponse;
import hu.bme.aut.crypto_casino_backend.exception.ResourceAlreadyExistsException;
import hu.bme.aut.crypto_casino_backend.exception.ResourceNotFoundException;
import hu.bme.aut.crypto_casino_backend.mapper.WalletMapper;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.model.UserWallet;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.casinovault.CasinoVault;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

	private final UserRepository userRepository;

	private final UserWalletRepository walletRepository;

	private final CasinoVault casinoVault;

	private final WalletMapper walletMapper;

	@Transactional
	public WalletResponse addWallet(Long userId, WalletRequest request) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		if (walletRepository.existsByAddress(request.getAddress())) {
			throw new ResourceAlreadyExistsException("Wallet address already registered: " + request.getAddress());
		}

		boolean isPrimary = request.getIsPrimary() != null && request.getIsPrimary();

		if (isPrimary) {
			walletRepository.findByUserIdAndIsPrimaryTrue(userId).ifPresent(wallet -> wallet.setIsPrimary(false));
		}

		if (walletRepository.findByUserId(userId).isEmpty()) {
			isPrimary = true;
		}

		UserWallet wallet = walletMapper.walletRequestToUserWallet(request, user);
		wallet.setIsPrimary(isPrimary);

		UserWallet savedWallet = walletRepository.save(wallet);

		return walletMapper.userWalletToWalletResponse(savedWallet);
	}

	@Transactional
	public WalletResponse setPrimaryWallet(Long userId, SetPrimaryRequest request) {
		UserWallet wallet = walletRepository.findById(request.getWalletId())
			.orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + request.getWalletId()));

		if (!wallet.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("Wallet does not belong to the user");
		}

		walletRepository.unsetPrimaryForAllExcept(userId, wallet.getId());

		wallet.setIsPrimary(true);
		UserWallet updatedWallet = walletRepository.save(wallet);

		return walletMapper.userWalletToWalletResponse(updatedWallet);
	}

	@Transactional(readOnly = true)
	public List<WalletResponse> getUserWallets(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new ResourceNotFoundException("User not found with id: " + userId);
		}

		List<UserWallet> wallets = walletRepository.findByUserId(userId);
		return walletMapper.userWalletsToWalletResponses(wallets);
	}

	@Transactional(readOnly = true)
	public BalanceResponse getWalletBalance(Long userId, Long walletId) {
		UserWallet wallet = walletRepository.findById(walletId)
			.orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + walletId));

		if (!wallet.getUser().getId().equals(userId)) {
			throw new IllegalArgumentException("Wallet does not belong to the user");
		}

		BigDecimal balance = getVaultBalance(wallet.getAddress());

		return walletMapper.toBalanceResponse(wallet, balance);
	}

	public BigDecimal getVaultBalance(String walletAddress) {
		try {
			BigInteger balance = casinoVault.getBalance(walletAddress).send();
			return new BigDecimal(balance).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN);
		}
		catch (Exception e) {
			log.error("Error getting vault balance for address: {}", walletAddress, e);
			throw new RuntimeException("Failed to get vault balance from blockchain", e);
		}
	}

}
