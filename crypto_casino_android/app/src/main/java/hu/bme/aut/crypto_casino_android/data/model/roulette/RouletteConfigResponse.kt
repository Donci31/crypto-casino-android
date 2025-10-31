package hu.bme.aut.crypto_casino_android.data.model.roulette

import java.math.BigDecimal

data class RouletteConfigResponse(
    val minBet: BigDecimal,
    val maxBet: BigDecimal,
    val houseEdge: Int,
    val isActive: Boolean
)
