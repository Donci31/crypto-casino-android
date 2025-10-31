package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.config.Web3jConfig;
import hu.bme.aut.crypto_casino_backend.exception.ResourceNotFoundException;
import hu.bme.aut.crypto_casino_backend.model.DiceResult;
import hu.bme.aut.crypto_casino_backend.model.GameSession;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.model.UserWallet;
import hu.bme.aut.crypto_casino_backend.repository.DiceResultRepository;
import hu.bme.aut.crypto_casino_backend.repository.GameSessionRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Hash;
import org.web3j.dice.Dice;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiceService {

	private static final String GAME_TYPE = "DICE";

	private final UserRepository userRepository;

	private final UserWalletRepository walletRepository;

	private final GameSessionRepository gameSessionRepository;

	private final DiceResultRepository diceResultRepository;

	private final Dice dice;

	private final org.web3j.casinovault.CasinoVault casinoVault;

	private final Web3jConfig web3jConfig;

	private final SecureRandom secureRandom = new SecureRandom();

	private final Map<Long, String> serverSeedStorage = new HashMap<>();

	@Transactional
	public CompletableFuture<DiceGameCreatedResponse> createGame(Long userId, BigDecimal betAmount, Integer prediction,
			DiceResult.BetType betType, String clientSeedHex) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		UserWallet primaryWallet = walletRepository.findByUserIdAndIsPrimaryTrue(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User does not have a primary wallet"));

		String walletAddress = primaryWallet.getAddress();

		validateBetParameters(betAmount, prediction, betType);

		String validatedClientSeed = validateAndNormalizeClientSeed(clientSeedHex);

		String serverSeed = generateServerSeed();
		String serverSeedHash = calculateSeedHash(serverSeed);

		GameSession gameSession = GameSession.builder()
			.user(user)
			.gameType(GAME_TYPE)
			.betAmount(betAmount)
			.winAmount(BigDecimal.ZERO)
			.isResolved(false)
			.build();

		GameSession savedSession = gameSessionRepository.save(gameSession);

		return createGameOnBlockchain(walletAddress, betAmount, prediction, betType, serverSeedHash,
				validatedClientSeed)
			.thenApply(gameId -> {
				try {
					serverSeedStorage.put(gameId, serverSeed);

					DiceResult diceResult = DiceResult.builder()
						.gameSession(savedSession)
						.gameId(BigInteger.valueOf(gameId))
						.prediction(prediction)
						.betType(betType)
						.serverSeedHash(serverSeedHash)
						.clientSeed(validatedClientSeed)
						.settled(false)
						.build();

					diceResultRepository.save(diceResult);

					return DiceGameCreatedResponse.builder()
						.gameId(gameId)
						.serverSeedHash(serverSeedHash)
						.prediction(prediction)
						.betType(betType)
						.betAmount(betAmount)
						.timestamp(LocalDateTime.now())
						.build();
				}
				catch (Exception e) {
					log.error("Error saving dice game", e);
					throw new RuntimeException("Error saving dice game", e);
				}
			});
	}

	@Transactional
	public CompletableFuture<DiceGameSettledResponse> settleGame(Long userId, Long gameId) {
		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

		String serverSeed = serverSeedStorage.get(gameId);
		if (serverSeed == null) {
			throw new IllegalStateException("Server seed not found for game: " + gameId);
		}

		return settleGameOnBlockchain(gameId, serverSeed).thenApply(result -> {
			try {
				DiceResult diceResult = diceResultRepository.findByGameSessionId(result.getGameSessionId())
					.orElseThrow(() -> new ResourceNotFoundException("Dice result not found"));

				diceResult.setServerSeed(serverSeed);
				diceResult.setResult(result.getResult());
				diceResult.setPayout(result.getPayout().multiply(BigDecimal.TEN.pow(18)).toBigInteger());
				diceResult.setSettled(true);

				diceResultRepository.save(diceResult);

				GameSession gameSession = diceResult.getGameSession();
				gameSession.setWinAmount(result.getPayout());
				gameSession.setIsResolved(true);
				gameSession.setResolvedAt(LocalDateTime.now());
				gameSessionRepository.save(gameSession);

				serverSeedStorage.remove(gameId);

				return DiceGameSettledResponse.builder()
					.gameId(gameId)
					.result(result.getResult())
					.payout(result.getPayout())
					.won(result.isWon())
					.serverSeed(serverSeed)
					.timestamp(LocalDateTime.now())
					.build();
			}
			catch (Exception e) {
				log.error("Error settling dice game", e);
				throw new RuntimeException("Error settling dice game", e);
			}
		});
	}

	public DiceConfigResponse getDiceConfig() {
		try {
			BigInteger minBet = dice.minBet().send();
			BigInteger maxBet = dice.maxBet().send();
			BigInteger houseEdge = dice.houseEdge().send();
			boolean isPaused = dice.paused().send();

			return DiceConfigResponse.builder()
				.minBet(new BigDecimal(minBet).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN))
				.maxBet(new BigDecimal(maxBet).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN))
				.houseEdge(houseEdge.intValue())
				.isActive(!isPaused)
				.build();
		}
		catch (Exception e) {
			log.error("Error getting dice config", e);
			throw new RuntimeException("Failed to get dice configuration", e);
		}
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

	@Transactional(readOnly = true)
	public DiceGameStatusResponse getGameStatus(Long userId, Long gameId) {
		DiceResult diceResult = diceResultRepository.findByGameSessionId(gameId)
			.orElseThrow(() -> new ResourceNotFoundException("Dice game not found"));

		return DiceGameStatusResponse.builder()
			.gameId(diceResult.getGameId().longValue())
			.prediction(diceResult.getPrediction())
			.betType(diceResult.getBetType())
			.serverSeedHash(diceResult.getServerSeedHash())
			.clientSeed(diceResult.getClientSeed())
			.result(diceResult.getResult())
			.payout(diceResult.getPayout() != null
					? new BigDecimal(diceResult.getPayout()).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN)
					: BigDecimal.ZERO)
			.settled(diceResult.getSettled())
			.serverSeed(diceResult.getServerSeed())
			.build();
	}

	private CompletableFuture<Long> createGameOnBlockchain(String userAddress, BigDecimal betAmount, Integer prediction,
			DiceResult.BetType betType, String serverSeedHash, String clientSeedHex) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				BigInteger betAmountWei = betAmount.multiply(BigDecimal.TEN.pow(18)).toBigInteger();

				byte[] serverSeedHashBytes = Numeric.hexStringToByteArray(serverSeedHash);
				byte[] clientSeedBytes = Numeric.hexStringToByteArray(clientSeedHex);

				log.debug("Creating game on blockchain - serverSeedHashBytes length: {}, clientSeedBytes length: {}",
						serverSeedHashBytes.length, clientSeedBytes.length);

				BigInteger betTypeValue = BigInteger.valueOf(betType.ordinal());

				TransactionReceipt receipt = dice
					.createGame(userAddress, serverSeedHashBytes, betAmountWei, BigInteger.valueOf(prediction),
							betTypeValue, clientSeedBytes)
					.send();

				var events = Dice.getGameCreatedEvents(receipt);
				if (events.isEmpty()) {
					throw new RuntimeException("No GameCreated event found in transaction");
				}

				return events.getFirst().gameId.longValue();
			}
			catch (Exception e) {
				log.error("Error creating game on blockchain", e);
				throw new RuntimeException("Failed to create game on blockchain", e);
			}
		});
	}

	private CompletableFuture<DiceSettleResultData> settleGameOnBlockchain(Long gameId, String serverSeed) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				byte[] serverSeedBytes = Numeric.hexStringToByteArray(serverSeed);

				TransactionReceipt receipt = dice.settleGame(BigInteger.valueOf(gameId), serverSeedBytes).send();

				var events = Dice.getGameSettledEvents(receipt);
				if (events.isEmpty()) {
					throw new RuntimeException("No GameSettled event found in transaction");
				}

				var event = events.getFirst();

				BigDecimal payout = new BigDecimal(event.payout).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN);

				return DiceSettleResultData.builder()
					.gameSessionId(gameId)
					.result(event.result.intValue())
					.payout(payout)
					.won(event.won)
					.build();
			}
			catch (Exception e) {
				log.error("Error settling game on blockchain", e);
				throw new RuntimeException("Failed to settle game on blockchain", e);
			}
		});
	}

	private String generateServerSeed() {
		byte[] randomBytes = new byte[32];
		secureRandom.nextBytes(randomBytes);
		return Numeric.toHexString(randomBytes);
	}

	private String calculateSeedHash(String seed) {
		byte[] seedBytes = Numeric.hexStringToByteArray(seed);
		byte[] hash = Hash.sha3(seedBytes);
		return Numeric.toHexString(hash);
	}

	private void validateBetParameters(BigDecimal betAmount, Integer prediction, DiceResult.BetType betType) {
		DiceConfigResponse config = getDiceConfig();

		if (betAmount.compareTo(config.getMinBet()) < 0) {
			throw new IllegalArgumentException("Bet amount is below minimum: " + config.getMinBet());
		}

		if (betAmount.compareTo(config.getMaxBet()) > 0) {
			throw new IllegalArgumentException("Bet amount is above maximum: " + config.getMaxBet());
		}

		if (!config.isActive()) {
			throw new IllegalStateException("Dice game is currently not active");
		}

		if (prediction < 1 || prediction > 100) {
			throw new IllegalArgumentException("Prediction must be between 1 and 100");
		}

		if (betType == DiceResult.BetType.ROLL_UNDER || betType == DiceResult.BetType.ROLL_OVER) {
			if (prediction < 2 || prediction > 99) {
				throw new IllegalArgumentException("Prediction for ROLL_UNDER/ROLL_OVER must be between 2 and 99");
			}
		}
	}

	private String validateAndNormalizeClientSeed(String clientSeedHex) {
		if (clientSeedHex == null || clientSeedHex.trim().isEmpty()) {
			return generateServerSeed();
		}

		String normalizedSeed = clientSeedHex.trim();
		if (!normalizedSeed.startsWith("0x")) {
			normalizedSeed = "0x" + normalizedSeed;
		}

		byte[] seedBytes = Numeric.hexStringToByteArray(normalizedSeed);
		if (seedBytes.length != 32) {
			throw new IllegalArgumentException("Client seed must be exactly 32 bytes (64 hex characters). Provided: "
					+ seedBytes.length + " bytes");
		}

		return normalizedSeed;
	}

	@lombok.Data
	@lombok.Builder
	public static class DiceGameCreatedResponse {

		private Long gameId;

		private String serverSeedHash;

		private Integer prediction;

		private DiceResult.BetType betType;

		private BigDecimal betAmount;

		private LocalDateTime timestamp;

	}

	@lombok.Data
	@lombok.Builder
	public static class DiceGameSettledResponse {

		private Long gameId;

		private Integer result;

		private BigDecimal payout;

		private boolean won;

		private String serverSeed;

		private LocalDateTime timestamp;

	}

	@lombok.Data
	@lombok.Builder
	public static class DiceGameStatusResponse {

		private Long gameId;

		private Integer prediction;

		private DiceResult.BetType betType;

		private String serverSeedHash;

		private String clientSeed;

		private Integer result;

		private BigDecimal payout;

		private Boolean settled;

		private String serverSeed;

	}

	@lombok.Data
	@lombok.Builder
	public static class DiceConfigResponse {

		private BigDecimal minBet;

		private BigDecimal maxBet;

		private Integer houseEdge;

		private boolean isActive;

	}

	@lombok.Data
	@lombok.Builder
	private static class DiceSettleResultData {

		private Long gameSessionId;

		private Integer result;

		private BigDecimal payout;

		private boolean won;

	}

}
