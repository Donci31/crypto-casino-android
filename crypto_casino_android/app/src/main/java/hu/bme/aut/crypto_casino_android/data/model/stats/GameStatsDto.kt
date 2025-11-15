package hu.bme.aut.crypto_casino_android.data.model.stats

data class GameStatsDto(
    val played: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double
)
