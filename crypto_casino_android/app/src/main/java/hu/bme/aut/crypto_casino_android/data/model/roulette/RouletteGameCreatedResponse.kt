package hu.bme.aut.crypto_casino_android.data.model.roulette

import java.math.BigDecimal

data class RouletteGameCreatedResponse(
    val gameId: Long,
    val serverSeedHash: String,
    val bets: List<RouletteBetRequest>,
    val totalBetAmount: BigDecimal
)
