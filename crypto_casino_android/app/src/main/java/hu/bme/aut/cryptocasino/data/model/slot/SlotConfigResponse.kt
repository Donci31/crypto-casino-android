package hu.bme.aut.cryptocasino.data.model.slot

import java.math.BigDecimal

data class SlotConfigResponse(
    val minBet: BigDecimal,
    val maxBet: BigDecimal,
    val houseEdge: Int,
)
