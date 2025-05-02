package hu.bme.aut.crypto_casino_android.data.model.slot

import java.math.BigDecimal
import java.time.LocalDateTime

data class SpinResponse(
    val spinId: Long,
    val reels: List<Int>,
    val betAmount: BigDecimal,
    val winAmount: BigDecimal,
    val timestamp: LocalDateTime
)