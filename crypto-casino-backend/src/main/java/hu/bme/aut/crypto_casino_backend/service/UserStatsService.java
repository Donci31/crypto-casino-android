package hu.bme.aut.crypto_casino_backend.service;

import hu.bme.aut.crypto_casino_backend.dto.stats.GameStatsDto;
import hu.bme.aut.crypto_casino_backend.dto.stats.QuickStatsResponse;
import hu.bme.aut.crypto_casino_backend.dto.stats.UserStatsResponse;
import hu.bme.aut.crypto_casino_backend.exception.ResourceNotFoundException;
import hu.bme.aut.crypto_casino_backend.model.BlockchainTransaction;
import hu.bme.aut.crypto_casino_backend.model.GameSession;
import hu.bme.aut.crypto_casino_backend.model.UserWallet;
import hu.bme.aut.crypto_casino_backend.repository.BlockchainTransactionRepository;
import hu.bme.aut.crypto_casino_backend.repository.GameSessionRepository;
import hu.bme.aut.crypto_casino_backend.repository.UserWalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.casinotoken.CasinoToken;
import org.web3j.casinovault.CasinoVault;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStatsService {

  private final UserWalletRepository walletRepository;

  private final GameSessionRepository gameSessionRepository;

  private final BlockchainTransactionRepository transactionRepository;

  private final CasinoToken casinoToken;

  private final CasinoVault casinoVault;

  @Transactional(readOnly = true)
  public UserStatsResponse getUserStats(Long userId) {
    UserWallet primaryWallet = walletRepository.findByUserIdAndIsPrimaryTrue(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User does not have a primary wallet"));

    String walletAddress = primaryWallet.getAddress();

    List<GameSession> allSessions = gameSessionRepository.findByUserId(userId);
    List<GameSession> resolvedSessions = allSessions.stream().filter(GameSession::getIsResolved).toList();

    Integer totalGamesPlayed = resolvedSessions.size();

    BigDecimal totalWinnings = resolvedSessions.stream()
        .map(GameSession::getWinAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalWagered = resolvedSessions.stream()
        .map(GameSession::getBetAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalLosses = totalWagered.subtract(totalWinnings);

    BigDecimal netProfitLoss = totalWinnings.subtract(totalWagered);

    long wins = resolvedSessions.stream().filter(session -> session.getWinAmount().compareTo(BigDecimal.ZERO) > 0)
        .count();

    Double winRate = totalGamesPlayed > 0 ? (double) wins / totalGamesPlayed : 0.0;

    BigDecimal biggestWin = resolvedSessions.stream()
        .map(GameSession::getWinAmount)
        .max(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);

    Map<String, Long> gameTypeCounts = resolvedSessions.stream()
        .collect(Collectors.groupingBy(GameSession::getGameType, Collectors.counting()));

    String mostPlayedGame = gameTypeCounts.entrySet()
        .stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElse(null);

    List<BlockchainTransaction> deposits = transactionRepository
        .findByUserAddressAndEventType(walletAddress.toLowerCase(), BlockchainTransaction.TransactionType.DEPOSIT);

    BigDecimal totalDeposited = deposits.stream()
        .map(BlockchainTransaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    List<BlockchainTransaction> withdrawals = transactionRepository.findByUserAddressAndEventType(
        walletAddress.toLowerCase(), BlockchainTransaction.TransactionType.WITHDRAWAL);

    BigDecimal totalWithdrawn = withdrawals.stream()
        .map(BlockchainTransaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal houseEdgePaid = totalWagered.subtract(totalWinnings).max(BigDecimal.ZERO);

    Map<String, GameStatsDto> gameStats = new HashMap<>();

    for (String gameType : gameTypeCounts.keySet()) {
      List<GameSession> gameSessions = resolvedSessions.stream()
          .filter(session -> session.getGameType().equals(gameType))
          .toList();

      int played = gameSessions.size();
      long gameWins = gameSessions.stream()
          .filter(session -> session.getWinAmount().compareTo(BigDecimal.ZERO) > 0)
          .count();
      int losses = played - (int) gameWins;
      double gameWinRate = played > 0 ? (double) gameWins / played : 0.0;

      gameStats.put(gameType,
          GameStatsDto.builder()
              .played(played)
              .wins((int) gameWins)
              .losses(losses)
              .winRate(gameWinRate)
              .build());
    }

    return UserStatsResponse.builder()
        .totalGamesPlayed(totalGamesPlayed)
        .winRate(winRate)
        .totalWinnings(totalWinnings)
        .totalLosses(totalLosses)
        .netProfitLoss(netProfitLoss)
        .biggestWin(biggestWin)
        .mostPlayedGame(mostPlayedGame)
        .totalDeposited(totalDeposited)
        .totalWithdrawn(totalWithdrawn)
        .totalWagered(totalWagered)
        .houseEdgePaid(houseEdgePaid)
        .gameStats(gameStats)
        .build();
  }

  @Transactional(readOnly = true)
  public QuickStatsResponse getQuickStats(Long userId) {
    UserWallet primaryWallet = walletRepository.findByUserIdAndIsPrimaryTrue(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User does not have a primary wallet"));

    String walletAddress = primaryWallet.getAddress();

    BigDecimal vaultBalance = getVaultBalance(walletAddress);
    BigDecimal walletBalance = getWalletBalance(walletAddress);

    List<GameSession> resolvedSessions = gameSessionRepository.findByUserId(userId)
        .stream()
        .filter(GameSession::getIsResolved)
        .toList();

    Integer totalGamesPlayed = resolvedSessions.size();

    BigDecimal totalWinnings = resolvedSessions.stream()
        .map(GameSession::getWinAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    long wins = resolvedSessions.stream().filter(session -> session.getWinAmount().compareTo(BigDecimal.ZERO) > 0)
        .count();

    Double winRate = totalGamesPlayed > 0 ? (double) wins / totalGamesPlayed : 0.0;

    BigDecimal biggestWin = resolvedSessions.stream()
        .map(GameSession::getWinAmount)
        .max(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);

    return QuickStatsResponse.builder()
        .vaultBalance(vaultBalance)
        .walletBalance(walletBalance)
        .totalGamesPlayed(totalGamesPlayed)
        .totalWinnings(totalWinnings)
        .winRate(winRate)
        .biggestWin(biggestWin)
        .build();
  }

  private BigDecimal getVaultBalance(String walletAddress) {
    try {
      BigInteger balance = casinoVault.getBalance(walletAddress).send();
      return new BigDecimal(balance).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN);
    } catch (Exception e) {
      log.error("Error getting vault balance from blockchain", e);
      return BigDecimal.ZERO;
    }
  }

  private BigDecimal getWalletBalance(String walletAddress) {
    try {
      BigInteger balance = casinoToken.balanceOf(walletAddress).send();
      return new BigDecimal(balance).divide(BigDecimal.TEN.pow(18), RoundingMode.DOWN);
    } catch (Exception e) {
      log.error("Error getting wallet balance from blockchain", e);
      return BigDecimal.ZERO;
    }
  }

}
