package hu.bme.aut.cryptocasino.data.model.stats

import java.math.BigDecimal

data class QuickStatsResponse(
    val vaultBalance: BigDecimal,
    val walletBalance: BigDecimal,
    val totalGamesPlayed: Int,
    val totalWinnings: BigDecimal,
    val winRate: Double,
    val biggestWin: BigDecimal,
)
