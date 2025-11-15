package hu.bme.aut.crypto_casino_android.data.model.stats

import java.math.BigDecimal

data class QuickStatsResponse(
    val vaultBalance: BigDecimal,
    val walletBalance: BigDecimal,
    val totalGamesPlayed: Int,
    val totalWinnings: BigDecimal,
    val winRate: Double,
    val biggestWin: BigDecimal
)
