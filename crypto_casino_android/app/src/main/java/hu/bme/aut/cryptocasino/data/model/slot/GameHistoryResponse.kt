package hu.bme.aut.cryptocasino.data.model.slot

import java.math.BigDecimal
import java.time.LocalDateTime

data class GameHistoryResponse(
    val id: Long,
    val spinId: Long,
    val gameType: String,
    val betAmount: BigDecimal,
    val winAmount: BigDecimal,
    val reels: List<Int>?,
    val timestamp: LocalDateTime,
)
