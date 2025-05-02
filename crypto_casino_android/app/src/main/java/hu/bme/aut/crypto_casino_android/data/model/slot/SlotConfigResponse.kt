package hu.bme.aut.crypto_casino_android.data.model.slot

import java.math.BigDecimal

data class SlotConfigResponse(
    val minBet: BigDecimal,
    val maxBet: BigDecimal,
    val houseEdge: Int // in percentage
)
