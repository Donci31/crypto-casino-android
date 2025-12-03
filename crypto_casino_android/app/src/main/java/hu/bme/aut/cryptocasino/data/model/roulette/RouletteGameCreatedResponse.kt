package hu.bme.aut.cryptocasino.data.model.roulette

import java.math.BigDecimal

data class RouletteGameCreatedResponse(
    val gameId: Long,
    val serverSeedHash: String,
    val transactionHash: String? = null,
    val blockNumber: Long? = null,
    val bets: List<RouletteBetRequest>,
    val totalBetAmount: BigDecimal,
)
