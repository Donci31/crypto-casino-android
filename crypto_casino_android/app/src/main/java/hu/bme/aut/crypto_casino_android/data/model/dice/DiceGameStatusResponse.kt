package hu.bme.aut.crypto_casino_android.data.model.dice

import java.math.BigDecimal
import java.time.LocalDateTime

data class DiceGameStatusResponse(
    val gameId: Long,
    val player: String,
    val betAmount: BigDecimal,
    val prediction: Int,
    val betType: BetType,
    val serverSeedHash: String,
    val result: Int?,
    val payout: BigDecimal?,
    val settled: Boolean,
    val createdAt: LocalDateTime,
    val settledAt: LocalDateTime?
)
