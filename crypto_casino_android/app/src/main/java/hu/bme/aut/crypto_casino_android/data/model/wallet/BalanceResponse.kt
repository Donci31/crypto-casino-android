package hu.bme.aut.crypto_casino_android.data.model.wallet

import java.math.BigDecimal

data class BalanceResponse(
    val address: String,
    val balance: BigDecimal
)
