package hu.bme.aut.cryptocasino.data.model.stats

data class GameStatsDto(
    val played: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double,
)
