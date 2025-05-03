package hu.bme.aut.crypto_casino_android.data.model.wallet

data class WalletRequest(
    val address: String,
    val label: String,
    val isPrimary: Boolean = false
)
