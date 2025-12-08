package hu.bme.aut.cryptocasino.data.model.dice

import java.math.BigDecimal

data class DiceGameRequest(
    val tempGameId: String,
    val betAmount: BigDecimal,
    val prediction: Int,
    val betType: BetType,
    val clientSeed: String,
)

enum class BetType {
    ROLL_UNDER,
    ROLL_OVER,
    EXACT,
}
