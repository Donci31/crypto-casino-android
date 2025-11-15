package hu.bme.aut.crypto_casino_android.data.model.stats

import java.math.BigDecimal

data class UserStatsResponse(
    val totalGamesPlayed: Int,
    val winRate: Double,
    val totalWinnings: BigDecimal,
    val totalLosses: BigDecimal,
    val netProfitLoss: BigDecimal,
    val biggestWin: BigDecimal,
    val mostPlayedGame: String?,
    val totalDeposited: BigDecimal,
    val totalWithdrawn: BigDecimal,
    val totalWagered: BigDecimal,
    val houseEdgePaid: BigDecimal,
    val gameStats: Map<String, GameStatsDto>
)
