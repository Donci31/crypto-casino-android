package hu.bme.aut.cryptocasino.data.model.dice

import java.math.BigDecimal

data class DiceGameSettledResponse(
    val gameId: Long,
    val result: Int,
    val payout: BigDecimal,
    val won: Boolean,
    val serverSeed: String? = null,
)
