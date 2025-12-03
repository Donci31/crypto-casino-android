package hu.bme.aut.cryptocasino.data.model.transaction

import java.math.BigDecimal
import java.time.LocalDateTime

data class BlockchainTransaction(
    val txHash: String,
    val blockNumber: Long,
    val logIndex: Int,
    val userAddress: String,
    val eventType: TransactionType,
    val amount: BigDecimal,
    val newBalance: BigDecimal,
    val gameAddress: String?,
    val timestamp: LocalDateTime,
)

enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    BET,
    WIN,
    TOKEN_PURCHASED,
    TOKEN_EXCHANGED,
}
