package hu.bme.aut.crypto_casino_android.data.model.dice

import java.math.BigDecimal

data class DiceGameRequest(
    val betAmount: BigDecimal,
    val prediction: Int,
    val betType: BetType,
    val clientSeed: String
)

enum class BetType {
    ROLL_UNDER,
    ROLL_OVER,
    EXACT
}
