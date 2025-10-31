package hu.bme.aut.crypto_casino_android.data.model.dice

import java.math.BigDecimal

data class DiceConfigResponse(
    val minBet: BigDecimal,
    val maxBet: BigDecimal,
    val houseEdge: Int
)
