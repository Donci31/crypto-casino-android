package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.exception.ResourceNotFoundException;
import hu.bme.aut.crypto_casino_backend.model.GameSession;
import hu.bme.aut.crypto_casino_backend.model.RouletteBet;
import hu.bme.aut.crypto_casino_backend.model.RouletteResult;
import hu.bme.aut.crypto_casino_backend.model.User;
import hu.bme.aut.crypto_casino_backend.model.UserWallet;
import hu.bme.aut.crypto_casino_backend.repository.GameSessionRepository;
import hu.bme.aut.crypto_casino_backend.repository.RouletteBetRepository;
import hu.bme.aut.crypto_casino_backend.repository.RouletteResultRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.roulette.Roulette;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouletteService {

  private static final String GAME_TYPE = "ROULETTE";

  private final UserRepository userRepository;

  private final UserWalletRepository walletRepository;

  private final GameSessionRepository gameSessionRepository;

  private final RouletteResultRepository rouletteResultRepository;

  private final RouletteBetRepository rouletteBetRepository;

  private final Roulette roulette;

  private final org.web3j.casinovault.CasinoVault casinoVault;

  private final SecureRandom secureRandom = new SecureRandom();

  private final Map<Long, String> serverSeedStorage = new HashMap<>();

  @Transactional
  public RouletteGameCreatedResponse createGame(Long userId, List<BetRequest> bets, String clientSeedHex) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

    UserWallet primaryWallet = walletRepository.findByUserIdAndIsPrimaryTrue(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User does not have a primary wallet"));

    String walletAddress = primaryWallet.getAddress();

    validateBets(bets);

    BigDecimal totalBetAmount = bets.stream().map(BetRequest::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

    String serverSeed = generateServerSeed();
    String serverSeedHash = calculateSeedHash(serverSeed);

    GameSession gameSession = GameSession.builder()
        .user(user)
        .gameType(GAME_TYPE)
        .betAmount(totalBetAmount)
        .winAmount(BigDecimal.ZERO)
        .isResolved(false)
        .build();

    GameSession savedSession = gameSessionRepository.save(gameSession);

    BlockchainGameCreationResult creationResult = createGameOnBlockchain(walletAddress, bets, serverSeedHash,
        clientSeedHex);

    Long gameId = creationResult.getGameId();
    serverSeedStorage.put(gameId, serverSeed);

    RouletteResult rouletteResult = RouletteResult.builder()
        .gameSession(savedSession)
        .gameId(BigInteger.valueOf(gameId))
        .totalBetAmount(totalBetAmount.multiply(BigDecimal.TEN.pow(18)).toBigInteger())
        .serverSeedHash(serverSeedHash)
        .clientSeed(clientSeedHex)
        .settled(false)
        .bets(new ArrayList<>())
        .build();

    RouletteResult savedResult = rouletteResultRepository.save(rouletteResult);

    List<RouletteBet> savedBets = bets.stream().map(betReq -> {
      RouletteBet bet = RouletteBet.builder()
          .rouletteResult(savedResult)
          .betType(betReq.getBetType())
          .amount(betReq.getAmount().multiply(BigDecimal.TEN.pow(18)).toBigInteger())
          .number(betReq.getNumber())
          .build();
      return rouletteBetRepository.save(bet);
    }).collect(Collectors.toList());

    savedResult.setBets(savedBets);

    return RouletteGameCreatedResponse.builder()
        .gameId(gameId)
        .serverSeedHash(serverSeedHash)
        .transactionHash(creationResult.getTransactionHash())
        .blockNumber(creationResult.getBlockNumber())
        .bets(bets)
        .totalBetAmount(totalBetAmount)
        .timestamp(LocalDateTime.now())
        .build();
  }

  @Transactional
  public RouletteGameSettledResponse settleGame(Long userId, Long gameId) {
    String serverSeed = serverSeedStorage.get(gameId);
    if (serverSeed == null) {
      throw new IllegalStateException("Server seed not found for game: " + gameId);
    }

    RouletteSettleResultData result = settleGameOnBlockchain(gameId, serverSeed);

    RouletteResult rouletteResult = rouletteResultRepository.findByGameId(BigInteger.valueOf(gameId))
        .orElseThrow(() -> new ResourceNotFoundException("Roulette result not found"));

    if (!rouletteResult.getGameSession().getUser().getId().equals(userId)) {
      throw new IllegalStateException("User is not authorized to settle this game");
    }

    rouletteResult.setServerSeed(serverSeed);
    rouletteResult.setWinningNumber(result.getWinningNumber());
    rouletteResult.setTotalPayout(result.getTotalPayout().multiply(BigDecimal.TEN.pow(18)).toBigInteger());
    rouletteResult.setSettled(true);

    rouletteResultRepository.save(rouletteResult);

    GameSession gameSession = rouletteResult.getGameSession();
    gameSession.setWinAmount(result.getTotalPayout());
    gameSession.setIsResolved(true);
    gameSession.setResolvedAt(LocalDateTime.now());
    gameSessionRepository.save(gameSession);

    serverSeedStorage.remove(gameId);

    return RouletteGameSettledResponse.builder()
        .gameId(gameId)
        .winningNumber(result.getWinningNumber())
        .totalPayout(result.getTotalPayout())
        .serverSeed(serverSeed)
        .timestamp(LocalDateTime.now())
        .build();
  }

  public RouletteConfigResponse getRouletteConfig() {
    try {
      BigInteger minBet = roulette.minBet().send();
      BigInteger maxBet = roulette.maxBet().send();
      BigInteger houseEdge = roulette.houseEdge().send();
      boolean isPaused = roulette.paused().send();

      return RouletteConfigResponse.builder()
          .minBet(new BigDecimal(minBet).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN))
          .maxBet(new BigDecimal(maxBet).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN))
          .houseEdge(houseEdge.intValue())
          .isActive(!isPaused)
          .build();
    } catch (Exception e) {
      log.error("Error getting roulette config", e);
      throw new RuntimeException("Failed to get roulette configuration", e);
    }
  }

  public BigDecimal getVaultBalance(String walletAddress) {
    try {
      BigInteger balance = casinoVault.getBalance(walletAddress).send();
      return new BigDecimal(balance).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN);
    } catch (Exception e) {
      log.error("Error getting balance from blockchain", e);
      throw new RuntimeException("Failed to get balance from blockchain", e);
    }
  }

  @Transactional(readOnly = true)
  public RouletteGameStatusResponse getGameStatus(Long userId, Long gameId) {
    RouletteResult rouletteResult = rouletteResultRepository.findByGameSessionId(gameId)
        .orElseThrow(() -> new ResourceNotFoundException("Roulette game not found"));

    if (!rouletteResult.getGameSession().getUser().getId().equals(userId)) {
      throw new IllegalStateException("User is not authorized to view this game");
    }

    List<BetStatusResponse> betsStatus = rouletteResult.getBets()
        .stream()
        .map(bet -> BetStatusResponse.builder()
            .betType(bet.getBetType())
            .amount(new BigDecimal(bet.getAmount()).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN))
            .number(bet.getNumber())
            .build())
        .collect(Collectors.toList());

    return RouletteGameStatusResponse.builder()
        .gameId(rouletteResult.getGameId().longValue())
        .bets(betsStatus)
        .serverSeedHash(rouletteResult.getServerSeedHash())
        .clientSeed(rouletteResult.getClientSeed())
        .winningNumber(rouletteResult.getWinningNumber())
        .totalPayout(rouletteResult.getTotalPayout() != null
            ? new BigDecimal(rouletteResult.getTotalPayout()).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN)
            : BigDecimal.ZERO)
        .settled(rouletteResult.getSettled())
        .serverSeed(rouletteResult.getServerSeed())
        .build();
  }

  private BlockchainGameCreationResult createGameOnBlockchain(String userAddress, List<BetRequest> bets,
      String serverSeedHash, String clientSeedHex) {
    try {
      List<Roulette.Bet> contractBets = bets.stream().map(betReq -> {
        BigInteger betAmountWei = betReq.getAmount().multiply(BigDecimal.TEN.pow(18)).toBigInteger();
        BigInteger betTypeValue = BigInteger.valueOf(betReq.getBetType().ordinal());
        BigInteger number = BigInteger.valueOf(betReq.getNumber());

        return new Roulette.Bet(betTypeValue, betAmountWei, number);
      }).collect(Collectors.toList());

      byte[] serverSeedHashBytes = Numeric.hexStringToByteArray(serverSeedHash);
      byte[] clientSeedBytes = Numeric.hexStringToByteArray(clientSeedHex);

      TransactionReceipt receipt = roulette
          .createGame(userAddress, serverSeedHashBytes, contractBets, clientSeedBytes)
          .send();

      var events = Roulette.getGameCreatedEvents(receipt);
      if (events.isEmpty()) {
        throw new RuntimeException("No GameCreated event found in transaction");
      }

      Long gameId = events.getFirst().gameId.longValue();
      String transactionHash = receipt.getTransactionHash();
      Long blockNumber = receipt.getBlockNumber().longValue();

      return BlockchainGameCreationResult.builder()
          .gameId(gameId)
          .transactionHash(transactionHash)
          .blockNumber(blockNumber)
          .build();
    } catch (Exception e) {
      log.error("Error creating roulette game on blockchain", e);
      throw new RuntimeException("Failed to create roulette game on blockchain", e);
    }
  }

  private RouletteSettleResultData settleGameOnBlockchain(Long gameId, String serverSeed) {
    try {
      byte[] serverSeedBytes = Numeric.hexStringToByteArray(serverSeed);

      TransactionReceipt receipt = roulette.settleGame(BigInteger.valueOf(gameId), serverSeedBytes).send();

      var events = Roulette.getGameSettledEvents(receipt);
      if (events.isEmpty()) {
        throw new RuntimeException("No GameSettled event found in transaction");
      }

      var event = events.getFirst();

      BigDecimal totalPayout = new BigDecimal(event.totalPayout).divide(BigDecimal.TEN.pow(18),
          RoundingMode.DOWN);

      return RouletteSettleResultData.builder()
          .gameSessionId(gameId)
          .winningNumber(event.winningNumber.intValue())
          .totalPayout(totalPayout)
          .build();
    } catch (Exception e) {
      log.error("Error settling roulette game on blockchain", e);
      throw new RuntimeException("Failed to settle roulette game on blockchain", e);
    }
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

  private void validateBets(List<BetRequest> bets) {
    RouletteConfigResponse config = getRouletteConfig();

    if (bets.isEmpty()) {
      throw new IllegalArgumentException("Must place at least one bet");
    }

    if (bets.size() > 20) {
      throw new IllegalArgumentException("Too many bets (max 20)");
    }

    if (!config.isActive()) {
      throw new IllegalStateException("Roulette game is currently not active");
    }

    for (BetRequest bet : bets) {
      if (bet.getAmount().compareTo(config.getMinBet()) < 0) {
        throw new IllegalArgumentException("Bet amount is below minimum: " + config.getMinBet());
      }

      if (bet.getAmount().compareTo(config.getMaxBet()) > 0) {
        throw new IllegalArgumentException("Bet amount is above maximum: " + config.getMaxBet());
      }

      if (bet.getBetType() == RouletteBet.BetType.STRAIGHT && (bet.getNumber() < 0 || bet.getNumber() > 36)) {
        throw new IllegalArgumentException("STRAIGHT bet number must be 0-36");
      }
    }
  }

  @lombok.Data
  @lombok.Builder
  public static class BetRequest {

    private RouletteBet.BetType betType;

    private BigDecimal amount;

    private Integer number;

  }

  @lombok.Data
  @lombok.Builder
  public static class RouletteGameCreatedResponse {

    private Long gameId;

    private String serverSeedHash;

    private String transactionHash;

    private Long blockNumber;

    private List<BetRequest> bets;

    private BigDecimal totalBetAmount;

    private LocalDateTime timestamp;

  }

  @lombok.Data
  @lombok.Builder
  public static class RouletteGameSettledResponse {

    private Long gameId;

    private Integer winningNumber;

    private BigDecimal totalPayout;

    private String serverSeed;

    private LocalDateTime timestamp;

  }

  @lombok.Data
  @lombok.Builder
  public static class RouletteGameStatusResponse {

    private Long gameId;

    private List<BetStatusResponse> bets;

    private String serverSeedHash;

    private String clientSeed;

    private Integer winningNumber;

    private BigDecimal totalPayout;

    private Boolean settled;

    private String serverSeed;

  }

  @lombok.Data
  @lombok.Builder
  public static class BetStatusResponse {

    private RouletteBet.BetType betType;

    private BigDecimal amount;

    private Integer number;

  }

  @lombok.Data
  @lombok.Builder
  public static class RouletteConfigResponse {

    private BigDecimal minBet;

    private BigDecimal maxBet;

    private Integer houseEdge;

    private boolean isActive;

  }

  @lombok.Data
  @lombok.Builder
  private static class RouletteSettleResultData {

    private Long gameSessionId;

    private Integer winningNumber;

    private BigDecimal totalPayout;

  }

  @lombok.Data
  @lombok.Builder
  private static class BlockchainGameCreationResult {

    private Long gameId;

    private String transactionHash;

    private Long blockNumber;

  }

}
