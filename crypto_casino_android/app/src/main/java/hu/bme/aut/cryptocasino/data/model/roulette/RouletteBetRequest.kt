package hu.bme.aut.cryptocasino.data.model.roulette

import java.math.BigDecimal

data class RouletteBetRequest(
    val betType: BetType,
    val amount: BigDecimal,
    val number: Int,
)
