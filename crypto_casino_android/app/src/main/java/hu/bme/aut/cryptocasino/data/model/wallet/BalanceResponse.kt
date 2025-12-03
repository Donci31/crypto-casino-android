package hu.bme.aut.cryptocasino.data.model.wallet

import java.math.BigDecimal

data class BalanceResponse(
    val address: String,
    val balance: BigDecimal,
)
