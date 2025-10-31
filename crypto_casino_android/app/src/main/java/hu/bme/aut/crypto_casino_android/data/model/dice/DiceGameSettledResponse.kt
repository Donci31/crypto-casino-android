package hu.bme.aut.crypto_casino_android.data.model.dice

import java.math.BigDecimal

data class DiceGameSettledResponse(
    val gameId: Long,
    val result: Int,
    val payout: BigDecimal,
    val won: Boolean
)
