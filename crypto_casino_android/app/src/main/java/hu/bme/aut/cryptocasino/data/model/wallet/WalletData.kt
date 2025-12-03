package hu.bme.aut.cryptocasino.data.model.wallet

import java.math.BigDecimal

data class WalletData(
    val address: String,
    val privateKey: String,
    val label: String,
    val isPrimary: Boolean = false,
    val id: Long? = null,
    val balance: BigDecimal? = null,
)
