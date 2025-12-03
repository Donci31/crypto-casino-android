package hu.bme.aut.cryptocasino.data.model.roulette

import java.math.BigDecimal

data class RouletteGameSettledResponse(
    val gameId: Long,
    val winningNumber: Int,
    val totalPayout: BigDecimal,
    val serverSeed: String,
)
