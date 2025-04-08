package hu.bme.aut.crypto_casino_android.data.model.wallet

import hu.bme.aut.crypto_casino_android.data.model.transaction.Transaction

data class Wallet(
    val id: Long? = null,
    val userId: Long? = null,
    val casinoTokenBalance: Double = 0.0,
    val walletAddress: String? = null,
    val blockchainSynced: Boolean = false,
    val recentTransactions: List<Transaction> = emptyList()
)
