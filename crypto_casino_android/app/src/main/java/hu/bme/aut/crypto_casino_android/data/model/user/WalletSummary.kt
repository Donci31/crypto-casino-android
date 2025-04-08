package hu.bme.aut.crypto_casino_android.data.model.user

import java.math.BigDecimal

data class WalletSummary(
    val id: Long? = null,
    val casinoTokenBalance: BigDecimal,
    val walletAddress: String? = null,
    val transactionCount: Int = 0
)
