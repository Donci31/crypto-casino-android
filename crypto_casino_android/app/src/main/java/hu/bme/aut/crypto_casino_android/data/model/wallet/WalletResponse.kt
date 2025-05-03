package hu.bme.aut.crypto_casino_android.data.model.wallet

import java.time.LocalDateTime

data class WalletResponse(
    val id: Long,
    val address: String,
    val label: String,
    val isPrimary: Boolean,
    val createdAt: LocalDateTime
)
