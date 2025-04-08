package hu.bme.aut.crypto_casino_android.data.model.transaction

import java.time.ZonedDateTime

data class Transaction(
    val id: Long? = null,
    val userId: Long? = null,
    val walletId: Long? = null,
    val amount: Double = 0.0,
    val type: TransactionType? = null,
    val transactionHash: String? = null,
    val blockNumber: Long? = null,
    val ethereumAmount: Double? = null,
    val casinoTokenAmount: Double? = null,
    val transactionTime: ZonedDateTime? = null,
    val status: TransactionStatus? = null
)

enum class TransactionType {
    DEPOSIT, WITHDRAWAL, PURCHASE, EXCHANGE
}

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED
}
