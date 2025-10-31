package hu.bme.aut.crypto_casino_android.data.model.roulette

import java.math.BigDecimal

data class RouletteGameSettledResponse(
    val gameId: Long,
    val winningNumber: Int,
    val totalPayout: BigDecimal,
    val serverSeed: String
)
