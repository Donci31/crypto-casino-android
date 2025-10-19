package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.config.Web3jConfig;
import hu.bme.aut.crypto_casino_backend.dto.game.GameHistoryResponse;
import hu.bme.aut.crypto_casino_backend.dto.game.SlotConfigResponse;
import hu.bme.aut.crypto_casino_backend.dto.game.SpinResponse;
import hu.bme.aut.crypto_casino_backend.exception.ResourceNotFoundException;
import hu.bme.aut.crypto_casino_backend.mapper.GameMapper;
import hu.bme.aut.crypto_casino_backend.model.GameSession;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.model.UserWallet;
import hu.bme.aut.crypto_casino_backend.model.SlotMachineResult;
import hu.bme.aut.crypto_casino_backend.repository.BlockchainTransactionRepository;
import hu.bme.aut.crypto_casino_backend.repository.GameSessionRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserWalletRepository;
import hu.bme.aut.crypto_casino_backend.repository.SlotMachineResultRepository;
import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.casinovault.CasinoVault;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.slotmachine.SlotMachine;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SlotMachineService {

	private static final String GAME_TYPE = "SLOT_MACHINE";

	private final UserRepository userRepository;

	private final UserWalletRepository walletRepository;

	private final GameSessionRepository gameSessionRepository;

	private final SlotMachineResultRepository slotMachineResultRepository;

	private final BlockchainTransactionRepository blockchainTransactionRepository;

	private final GameMapper gameMapper;

	private final SlotMachine slotMachine;

	private final CasinoVault casinoVault;

	private final Web3jConfig web3jConfig;

	@Transactional
	public CompletableFuture<SpinResponse> executeSpin(Long userId, BigDecimal betAmount) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		UserWallet primaryWallet = walletRepository.findByUserIdAndIsPrimaryTrue(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User does not have a primary wallet"));

		String walletAddress = primaryWallet.getAddress();

		SlotConfigResponse config = getSlotMachineConfig();

		if (betAmount.compareTo(config.getMinBet()) < 0) {
			throw new IllegalArgumentException("Bet amount is below minimum: " + config.getMinBet());
		}

		if (betAmount.compareTo(config.getMaxBet()) > 0) {
			throw new IllegalArgumentException("Bet amount is above maximum: " + config.getMaxBet());
		}

		if (!config.isActive()) {
			throw new IllegalStateException("Slot machine is currently not active");
		}

		GameSession gameSession = GameSession.builder()
			.user(user)
			.gameType(GAME_TYPE)
			.betAmount(betAmount)
			.winAmount(BigDecimal.ZERO)
			.isResolved(false)
			.build();

		GameSession savedSession = gameSessionRepository.save(gameSession);

		return executeSpinOnBlockchain(walletAddress, betAmount).thenApply(result -> {
			try {
				savedSession.setWinAmount(result.getWinAmount());
				savedSession.setIsResolved(true);
				savedSession.setResolvedAt(LocalDateTime.now());

				GameSession updatedSession = gameSessionRepository.save(savedSession);

				SlotMachineResult slotResult = SlotMachineResult.builder()
					.gameSession(updatedSession)
					.reel1(result.getReels()[0])
					.reel2(result.getReels()[1])
					.reel3(result.getReels()[2])
					.spinId(result.getSpinId())
					.build();

				slotMachineResultRepository.save(slotResult);

				BigDecimal newBalance = getVaultBalance(walletAddress);

				return SpinResponse.builder()
					.spinId(result.getSpinId())
					.reels(result.getReels())
					.betAmount(betAmount)
					.winAmount(result.getWinAmount())
					.isWin(result.isWin())
					.timestamp(LocalDateTime.now())
					.newBalance(newBalance)
					.build();
			}
			catch (Exception e) {
				log.error("Error processing spin result", e);
				throw new RuntimeException("Error processing spin result", e);
			}
		});
	}

	private CompletableFuture<SpinResultData> executeSpinOnBlockchain(String userAddress, BigDecimal betAmount) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				BigInteger betAmountWei = betAmount.multiply(BigDecimal.TEN.pow(18)).toBigInteger();

				TransactionReceipt receipt = slotMachine.spinForPlayer(userAddress, betAmountWei).send();

				List<SlotMachine.SpinResultEventResponse> results = SlotMachine.getSpinResultEvents(receipt);
				if (results.isEmpty()) {
					throw new RuntimeException("No spin result event found in transaction");
				}

				var event = results.getFirst();

				int[] reels = new int[3];
				reels[0] = event.reels.get(0).intValue();
				reels[1] = event.reels.get(1).intValue();
				reels[2] = event.reels.get(2).intValue();

				BigDecimal winAmount = new BigDecimal(event.winAmount).divide(BigDecimal.TEN.pow(18),
						RoundingMode.DOWN);

				BlockchainTransaction betTx = BlockchainTransaction.builder()
					.txHash(receipt.getTransactionHash())
					.blockNumber(receipt.getBlockNumber().longValue())
					.userAddress(userAddress)
					.eventType(BlockchainTransaction.TransactionType.BET)
					.amount(betAmount)
					.gameAddress(web3jConfig.getSlotMachineAddress())
					.timestamp(LocalDateTime.now())
					.build();

				blockchainTransactionRepository.save(betTx);

				if (winAmount.compareTo(BigDecimal.ZERO) > 0) {
					BlockchainTransaction winTx = BlockchainTransaction.builder()
						.txHash(receipt.getTransactionHash())
						.blockNumber(receipt.getBlockNumber().longValue())
						.logIndex(event.log.getLogIndex().intValue())
						.userAddress(userAddress)
						.eventType(BlockchainTransaction.TransactionType.WIN)
						.amount(winAmount)
						.gameAddress(web3jConfig.getSlotMachineAddress())
						.timestamp(LocalDateTime.now())
						.build();

					blockchainTransactionRepository.save(winTx);
				}

				return SpinResultData.builder()
					.spinId(event.spinId.longValue())
					.reels(reels)
					.betAmount(betAmount)
					.winAmount(winAmount)
					.isWin(winAmount.compareTo(BigDecimal.ZERO) > 0)
					.build();

			}
			catch (Exception e) {
				log.error("Error executing spin on blockchain", e);
				throw new RuntimeException("Failed to execute spin on blockchain", e);
			}
		});
	}

	public BigDecimal getVaultBalance(String walletAddress) {
		try {
			BigInteger balance = casinoVault.getBalance(walletAddress).send();
			return new BigDecimal(balance).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN);
		}
		catch (Exception e) {
			log.error("Error getting balance from blockchain", e);
			throw new RuntimeException("Failed to get balance from blockchain", e);
		}
	}

	public SlotConfigResponse getSlotMachineConfig() {
		try {
			BigInteger minBet = slotMachine.minBet().send();
			BigInteger maxBet = slotMachine.maxBet().send();
			BigInteger houseEdge = slotMachine.houseEdge().send();
			boolean isPaused = slotMachine.paused().send();

			return SlotConfigResponse.builder()
				.minBet(new BigDecimal(minBet).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN))
				.maxBet(new BigDecimal(maxBet).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN))
				.houseEdge(houseEdge.intValue())
				.isActive(!isPaused)
				.build();

		}
		catch (Exception e) {
			log.error("Error getting slot machine config", e);
			throw new RuntimeException("Failed to get slot machine configuration", e);
		}
	}

	@Transactional(readOnly = true)
	public List<GameHistoryResponse> getGameHistory(Long userId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		List<GameSession> sessions = gameSessionRepository.findTop10ByUserOrderByCreatedAtDesc(user);

		return sessions.stream().filter(GameSession::getIsResolved).map(session -> {
			try {
				GameHistoryResponse response = gameMapper.gameSessionToHistoryResponse(session);

				SlotMachineResult result = slotMachineResultRepository.findByGameSessionId(session.getId())
					.orElse(null);

				if (result != null) {
					response.setSpinId(result.getSpinId());
					response.setReels(new int[] { result.getReel1(), result.getReel2(), result.getReel3() });
				}

				return response;
			}
			catch (Exception e) {
				log.error("Error parsing game result", e);
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@lombok.Data
	@lombok.Builder
	@lombok.AllArgsConstructor
	@lombok.NoArgsConstructor
	public static class SpinResultData {

		private Long spinId;

		private int[] reels;

		private BigDecimal betAmount;

		private BigDecimal winAmount;

		private boolean isWin;

	}

}
