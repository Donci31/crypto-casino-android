package hu.bme.aut.crypto_casino_android.data.model.wallet

import java.math.BigDecimal

data class WalletData(
    val address: String,
    val privateKey: String,
    val label: String,
    val isPrimary: Boolean = false,
    val id: Long? = null,
    val balance: BigDecimal? = null
)
