package hu.bme.aut.cryptocasino.data.model.roulette

import java.math.BigDecimal

data class RouletteGameStatusResponse(
    val gameId: Long,
    val bets: List<BetStatusResponse>,
    val serverSeedHash: String,
    val clientSeed: String,
    val winningNumber: Int?,
    val totalPayout: BigDecimal,
    val settled: Boolean,
    val serverSeed: String?,
)

data class BetStatusResponse(
    val betType: BetType,
    val amount: BigDecimal,
    val number: Int,
)
